// -----------------------------------------------------------------------------
// Constants
// -----------------------------------------------------------------------------

const BOARD_SIZE = 10;
const LETTERS = "ABCDEFGHIJ";
const HEADER_OFFSET = 1;
const NO_COORD = -1;

const CELL_TEXT = {
    water: "~",
    ship: "#",
    hit: "X",
    miss: "O",
    sunk: "☠"
};

const GAME_STATUS_IN_PROGRESS = "IN_PROGRESS";

const SUNK_GLOW_COLOR = "#f1c40f";
const SUNK_GLOW_WIDTH = 3; // px
const SUNK_GLOW_PX = `${SUNK_GLOW_WIDTH}px`;

const API_PATHS = {
    attack: "api/battleship/attack",
    placeShip: "api/battleship/place-ship",
    startGame: "api/battleship/start-game",
    placementStart: "api/battleship/placement-start",
    version: "api/version"
};

const BOARD_IDS = {
    human: "human-board",
    computer: "computer-board"
};

const ELEMENT_IDS = {
    message: "message",
    computerMessage: "computer-message",
    guessesLeft: "guesses-left",
    version: "version",
    placementUi: "placement-ui",
    placementLabel: "placement-label",
    confirmPlacement: "confirmPlacement",
    toggleOrientation: "toggleOrientation"
};

// User Feedback button
const USER_FEEDBACK_LINK =
    "https://docs.google.com/forms/d/e/1FAIpQLSdr2xzx1jbwUW6hDL321aXq4rXhb8n56_qPCpv8hvU4RCcTCA/viewform";

/**
 * Tracks whether the game has ended (WIN or LOSS).
 * Used to block further attacks after the game is over.
 */
let gameOver = false;

// Placement phase state
const FLEET = [5, 4, 3, 3, 2];
const FLEET_NAMES = ["Carrier", "Battleship", "Submarine", "Submarine", "Patrol Boat"];
let placementIndex = 0;
let placementHorizontal = true;
let placementComplete = false;

// -----------------------------------------------------------------------------
// UI actions
// -----------------------------------------------------------------------------

function handleFeedbackClick() {
    window.open(USER_FEEDBACK_LINK, "_blank");
}

/**
 * Builds a board inside the given table element.
 * @param {string} tableId - The ID of the table element to populate.
 * @param {boolean} clickable - If true, each cell fires submitAttack on click.
 */
function loadBoard(tableId, clickable) {
    const table = document.getElementById(tableId);
    table.innerHTML = "";

    // Top header row (A-J)
    const headerRow = table.insertRow();
    headerRow.insertCell(); // empty corner
    for (let j = 0; j < BOARD_SIZE; j++) {
        const th = document.createElement("th");
        th.textContent = LETTERS[j];
        th.className = "label";
        headerRow.appendChild(th);
    }

    for (let i = 0; i < BOARD_SIZE; i++) {
        const row = table.insertRow();
        const labelCell = document.createElement("th");
        labelCell.textContent = String(i + HEADER_OFFSET);
        labelCell.className = "label";
        row.appendChild(labelCell);

        for (let j = 0; j < BOARD_SIZE; j++) {
            const cell = row.insertCell();
            cell.className = "water";
            cell.textContent = CELL_TEXT.water;
            if (clickable) {
                cell.onclick = function () {
                    submitAttack(i, j).catch(console.error);
                };
            }
        }
    }
}

/**
 * State-driven board renderer.
 * Renders every cell from provided grid state.
 *
 * @param {string} tableId
 * @param {string[][]} grid
 */
function renderBoard(tableId, grid) {
    const table = document.getElementById(tableId);

    for (let r = 0; r < grid.length; r++) {
        for (let c = 0; c < grid[r].length; c++) {
            const cell = table.rows[r + HEADER_OFFSET].cells[c + HEADER_OFFSET];
            const state = String(grid[r][c] || "").toLowerCase();

            // Preserve sunk icon if already marked and current state is still hit
            const wasSunk = cell.classList.contains("sunk");

            cell.className = state;
            if (wasSunk && state === "hit") {
                cell.classList.add("sunk");
            }

            if (state === "hit") {
                cell.textContent = wasSunk ? CELL_TEXT.sunk : CELL_TEXT.hit;
            } else if (state === "miss") {
                cell.textContent = CELL_TEXT.miss;
            } else if (state === "ship") {
                cell.textContent = CELL_TEXT.ship;
            } else {
                cell.textContent = CELL_TEXT.water;
            }
        }
    }
}

/**
 * @typedef {Object} AttackResponse
 * @property {boolean} isError
 * @property {string[][]} grid
 * @property {string[][]} homeGrid
 * @property {number} computerRow
 * @property {number} computerCol
 * @property {number} guessesLeft
 * @property {string} message
 * @property {string} computerMessage
 * @property {string} gameStatus
 * @property {number[][]|null} sunkCells
 * @property {number[][]|null} homeSunkCells
 */

async function submitAttack(row, col) {
    if (gameOver) return;

    try {
        const response = await fetch(API_PATHS.attack, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify({ row, column: col })
        });

        const raw = await response.text();
        let data;
        try {
            data = JSON.parse(raw);
        } catch {
            data = null;
        }

        // errors via HTTP status + ProblemDetail
        if (!response.ok) {
            document.getElementById(ELEMENT_IDS.message).innerText =
                data?.detail ?? `Request failed with status ${response.status}`;
            document.getElementById(ELEMENT_IDS.computerMessage).innerText = "";
            return;
        }

        // Render full board state from response
        if (!data) {
            document.getElementById(ELEMENT_IDS.message).innerText = "Empty response from server.";
            document.getElementById(ELEMENT_IDS.computerMessage).innerText = "";
            return;
        }

        renderBoard(BOARD_IDS.computer, data.grid ?? []);
        renderBoard(BOARD_IDS.human, data.homeGrid ?? []);

        if (data.computerRow !== NO_COORD) {
            highlightLastComputerMove(data.computerRow, data.computerCol);
        }

        if (data.sunkCells) markSunkCells(BOARD_IDS.computer, data.sunkCells);
        if (data.homeSunkCells) markSunkCells(BOARD_IDS.human, data.homeSunkCells);

        document.getElementById(ELEMENT_IDS.guessesLeft).innerText = `Guesses left: ${data.guessesLeft}`;
        document.getElementById(ELEMENT_IDS.message).innerText = data.message;
        document.getElementById(ELEMENT_IDS.computerMessage).innerText = data.computerMessage || "";

        if (data.gameStatus !== GAME_STATUS_IN_PROGRESS) {
            gameOver = true;
        }
    } catch (error) {
        document.getElementById(ELEMENT_IDS.message).innerText = "Network error. Please try again.";
        document.getElementById(ELEMENT_IDS.computerMessage).innerText = "";
        console.error(error);
    }
}

function markSunkCells(tableId, cells) {
    const table = document.getElementById(tableId);

    for (const [r, c] of cells) {
        const cell = table.rows[r + HEADER_OFFSET].cells[c + HEADER_OFFSET];
        cell.classList.add("sunk");
        cell.textContent = CELL_TEXT.sunk;
    }

    cleanSunkBorders(tableId, cells);
}

function cleanSunkBorders(tableId, cells) {
    const table = document.getElementById(tableId);
    const set = new Set(cells.map(([r, c]) => `${r},${c}`));

    for (const [r, c] of cells) {
        const cell = table.rows[r + HEADER_OFFSET].cells[c + HEADER_OFFSET];
        const shadows = [];

        if (!set.has(`${r - HEADER_OFFSET},${c}`)) {
            shadows.push(`inset 0 ${SUNK_GLOW_PX} 0 0 ${SUNK_GLOW_COLOR}`); // top
        }
        if (!set.has(`${r + HEADER_OFFSET},${c}`)) {
            shadows.push(`inset 0 -${SUNK_GLOW_PX} 0 0 ${SUNK_GLOW_COLOR}`); // bottom
        }
        if (!set.has(`${r},${c - HEADER_OFFSET}`)) {
            shadows.push(`inset ${SUNK_GLOW_PX} 0 0 0 ${SUNK_GLOW_COLOR}`); // left
        }
        if (!set.has(`${r},${c + HEADER_OFFSET}`)) {
            shadows.push(`inset -${SUNK_GLOW_PX} 0 0 0 ${SUNK_GLOW_COLOR}`); // right
        }

        cell.style.boxShadow = shadows.join(", ");
    }
}

function highlightLastComputerMove(row, col) {
    const prev = document.querySelector(".last-computer-move");
    if (prev) prev.classList.remove("last-computer-move");

    const table = document.getElementById(BOARD_IDS.human);
    table.rows[row + HEADER_OFFSET].cells[col + HEADER_OFFSET].classList.add("last-computer-move");
}

async function loadVersion() {
    try {
        const response = await fetch(API_PATHS.version);

        if (!response.ok) {
            document.getElementById(ELEMENT_IDS.version).innerText = `Version: unavailable`;
            return;
        }

        const version = await response.text();
        document.getElementById(ELEMENT_IDS.version).innerText = `Version: ${version}`;
    } catch (error) {
        document.getElementById(ELEMENT_IDS.version).innerText = `Version: unavailable`;
        console.error(error);
    }
}

function toggleOrientation() {
    placementHorizontal = !placementHorizontal;
    document.getElementById(ELEMENT_IDS.toggleOrientation).textContent =
        placementHorizontal ? "Orientation: Horizontal" : "Orientation: Vertical";
}

async function placeShipAtCell(row, col) {
    if (placementComplete) return;

    try {
        const response = await fetch(API_PATHS.placeShip, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                row,
                col,
                shipLength: FLEET[placementIndex],
                horizontal: placementHorizontal
            })
        });

        let data;
        try {
            data = await response.json();
        } catch {
            data = null;
        }

        if (!response.ok) {
            document.getElementById(ELEMENT_IDS.computerMessage).innerText =
                data?.detail ?? `Request failed with status ${response.status}`;
            return;
        }

        if (!data) {
            document.getElementById(ELEMENT_IDS.computerMessage).innerText = "Empty response from server.";
            return;
        }

        renderBoard(BOARD_IDS.human, data.homeGrid ?? []);
        document.getElementById(ELEMENT_IDS.computerMessage).innerText = "";
        placementIndex++;

        if (data.gameStatus === GAME_STATUS_IN_PROGRESS) {
            placementComplete = true;
            document.getElementById(ELEMENT_IDS.placementLabel).innerText = "All ships placed! Press Start Game.";
            document.getElementById(ELEMENT_IDS.confirmPlacement).disabled = false;
        } else {
            document.getElementById(ELEMENT_IDS.placementLabel).innerText =
                `Place your ${FLEET_NAMES[placementIndex]} (${FLEET[placementIndex]} cells)`;
        }
    } catch (error) {
        document.getElementById(ELEMENT_IDS.computerMessage).innerText = "Network error. Please try again.";
        console.error(error);
    }
}

async function confirmPlacement() {
    try {
        const response = await fetch(API_PATHS.startGame, { method: "POST" });

        if (!response.ok) {
            let detail = `Request failed with status ${response.status}`;
            try {
                const err = await response.json();
                detail = err?.detail || detail;
            } catch {
                // ignore parse errors
            }
            document.getElementById(ELEMENT_IDS.message).innerText = detail;
            return;
        }

        const guessesLeft = await response.json();

        document.getElementById(ELEMENT_IDS.guessesLeft).innerText = `Guesses left: ${guessesLeft}`;
        document.getElementById(ELEMENT_IDS.placementUi).style.display = "none";

        loadBoard(BOARD_IDS.computer, true);
    } catch (error) {
        document.getElementById(ELEMENT_IDS.message).innerText = "Network error. Please try again.";
        console.error(error);
    }
}

// On page load: start a new game, build both boards, and show the player's ships
window.onload = async function () {
    try {
        const response = await fetch(API_PATHS.placementStart, { method: "POST" });

        if (!response.ok) {
            let detail = `Request failed with status ${response.status}`;
            try {
                const err = await response.json();
                detail = err?.detail || detail;
            } catch {
                // ignore parse errors
            }
            document.getElementById(ELEMENT_IDS.message).innerText = detail;
            return;
        }

        loadBoard(BOARD_IDS.human, false);
        loadBoard(BOARD_IDS.computer, false);

        const humanTable = document.getElementById(BOARD_IDS.human);
        for (let i = 0; i < BOARD_SIZE; i++) {
            for (let j = 0; j < BOARD_SIZE; j++) {
                humanTable.rows[i + HEADER_OFFSET].cells[j + HEADER_OFFSET].onclick = function () {
                    placeShipAtCell(i, j).catch(console.error);
                };
            }
        }

        await loadVersion();
    } catch (error) {
        document.getElementById(ELEMENT_IDS.message).innerText = "Network error. Please try again.";
        console.error(error);
    }
};