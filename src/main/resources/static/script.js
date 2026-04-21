// Size of the board and column labels
const size = 10;
const letters = "ABCDEFGHIJ";

/**
 * Tracks whether the game has ended (WIN or LOSS).
 * Used to block further attacks after the game is over.
 */
let gameOver = false;

// User Feedback button
const USER_FEEDBACK_LINK = 'https://docs.google.com/forms/d/e/1FAIpQLSdr2xzx1jbwUW6hDL321aXq4rXhb8n56_qPCpv8hvU4RCcTCA/viewform';
function handleFeedbackClick() {
    window.open(USER_FEEDBACK_LINK, '_blank');
}

/**
 * Builds a 10x10 board inside the given table element.
 * @param {string} tableId - The ID of the table element to populate.
 * @param {boolean} clickable - If true, each cell fires submitAttack on click.
 */
function loadBoard(tableId, clickable) {
    const table = document.getElementById(tableId);
    table.innerHTML = '';

    // Top header row (A-J)
    const headerRow = table.insertRow();
    headerRow.insertCell(); // empty corner
    for (let j = 0; j < size; j++) {
        const th = document.createElement("th");
        th.textContent = letters[j];
        th.className = "label";
        headerRow.appendChild(th);
    }

    for (let i = 0; i < size; i++) {
        const row = table.insertRow();
        const labelCell = document.createElement("th");
        labelCell.textContent = String(i + 1);
        labelCell.className = "label";
        row.appendChild(labelCell);

        for (let j = 0; j < size; j++) {
            const cell = row.insertCell();
            cell.className = "water";
            cell.textContent = "~";
            if (clickable) {
                cell.onclick = function () {
                    submitAttack(i, j).catch(console.error);
                };
            }
        }
    }
}

/**
 * Sends the player's chosen cell to the backend and updates the UI.
 * Handles both the player's attack result and the computer's counter-move.
 * Ends the game if the backend returns a non-IN_PROGRESS status.
 *
 * @param {number} row - Zero-based row index of the attacked cell.
 * @param {number} col - Zero-based column index of the attacked cell.
 */

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
 * @property {number[][]|null} sunkCells        - Coords of the computer ship just sunk, or null
 * @property {number[][]|null} homeSunkCells     - Coords of the player ship just sunk, or null
 */

async function submitAttack(row, col) {
    if (gameOver) return;

    const response = await fetch("api/battleship/attack", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ row: row, column: col })
    });

    /** @type {AttackResponse} */
    const data = await response.json();

    if (data.isError) {
        document.getElementById("message").innerText = "Cell already attacked.";
        return;
    }

    // Update the cell the player just attacked
    updateComputerBoardCell(row, col, data.grid[row][col]);

    // Repaint the whole human board and highlight the computer's move
    renderHumanBoard(data.homeGrid);
    if (data.computerRow >= 0) {
        highlightLastComputerMove(data.computerRow, data.computerCol);
    }

    // Apply sunk styling to destroyed ships
    if (data.sunkCells) {
        markSunkCells("computer-board", data.sunkCells);
    }
    if (data.homeSunkCells) {
        markSunkCells("human-board", data.homeSunkCells);
    }

    document.getElementById("guesses-left").innerText = `Guesses left: ${data.guessesLeft}`;
    document.getElementById("message").innerText = data.message;
    document.getElementById("computer-message").innerText = data.computerMessage || "";

    if (data.gameStatus !== "IN_PROGRESS") {
        gameOver = true;
    }
}

/**
 * Updates a single cell on the computer's board after an attack.
 * @param {number} row - Zero-based row index.
 * @param {number} col - Zero-based column index.
 * @param {string} newState - "hit" or "miss" from the backend.
 */
function updateComputerBoardCell(row, col, newState) {
    const table = document.getElementById("computer-board");
    const cell = table.rows[row + 1].cells[col + 1];
    if (newState === "hit") {
        cell.className = "hit";
        cell.textContent = "X";
    } else {
        cell.className = "miss";
        cell.textContent = "O";
    }
}

/**
 * Repaints the entire human board based on the latest grid state from the backend.
 * @param {string[][]} homeGrid - 2D array of cell states: "hit", "miss", "ship", or "water".
 */
function renderHumanBoard(homeGrid) {
    const table = document.getElementById("human-board");
    for (let r = 0; r < homeGrid.length; r++) {
        for (let c = 0; c < homeGrid[r].length; c++) {
            const cell = table.rows[r + 1].cells[c + 1];
            const state = homeGrid[r][c].toLowerCase();
            // Preserve sunk class if the cell was already marked sunk
            const wasSunk = cell.classList.contains("sunk");
            cell.className = state;
            if (wasSunk && (state === "hit")) cell.classList.add("sunk");

            if (state === "hit") {
                // If it was already marked sunk, keep the special character
                if (wasSunk) {
                    cell.classList.add("sunk");
                    cell.textContent = "☠";
                } else {
                    cell.textContent = "X";
                }
            }
            else if (state === "miss") cell.textContent = "O";
            else if (state === "ship") cell.textContent = "#";
            else                       cell.textContent = "~";
        }
    }
}

/**
 * Applies the "sunk" class to every cell belonging to a just-destroyed ship.
 * The sunk class adds a black/yellow glow so the entire wreck is visually distinct.
 *
 * @param {string} tableId  - "computer-board" or "human-board"
 * @param {number[][]} cells - Array of [row, col] pairs for the sunk ship's cells
 */
// function markSunkCells(tableId, cells) {
//     const table = document.getElementById(tableId);
//     for (const [r, c] of cells) {
//         const cell = table.rows[r + 1].cells[c + 1];
//         cell.classList.add("sunk");
//         cell.textContent = "☠";
//     }
// }

function markSunkCells(tableId, cells) {
    const table = document.getElementById(tableId);

    for (const [r, c] of cells) {
        const cell = table.rows[r + 1].cells[c + 1];
        cell.classList.add("sunk");
        cell.textContent = "☠";
    }

    cleanSunkBorders(tableId, cells);
}

// function cleanSunkBorders(tableId, cells) {
//     const table = document.getElementById(tableId);
//     const set = new Set(cells.map(([r, c]) => `${r},${c}`));
//
//     for (const [r, c] of cells) {
//         const cell = table.rows[r + 1].cells[c + 1];
//
//         // Reset all sides to yellow outer glow border first
//         cell.style.borderTop    = "2px solid #f1c40f";
//         cell.style.borderBottom = "2px solid #f1c40f";
//         cell.style.borderLeft   = "2px solid #f1c40f";
//         cell.style.borderRight  = "2px solid #f1c40f";
//
//         // Kill shared inner edges between sunk cells
//         if (set.has(`${r - 1},${c}`)) cell.style.borderTop    = "1px solid #333";
//         if (set.has(`${r + 1},${c}`)) cell.style.borderBottom = "1px solid #333";
//         if (set.has(`${r},${c - 1}`)) cell.style.borderLeft   = "1px solid #333";
//         if (set.has(`${r},${c + 1}`)) cell.style.borderRight  = "1px solid #333";
//     }
// }

function cleanSunkBorders(tableId, cells) {
    const table = document.getElementById(tableId);
    const set = new Set(cells.map(([r, c]) => `${r},${c}`));
    const glow = "3px solid #f1c40f";

    for (const [r, c] of cells) {
        const cell = table.rows[r + 1].cells[c + 1];
        const shadows = [];

        // Add an inset shadow only on sides that face outward (no sunk neighbor)
        if (!set.has(`${r - 1},${c}`)) shadows.push("inset 0  3px 0 0 #f1c40f");  // top
        if (!set.has(`${r + 1},${c}`)) shadows.push("inset 0 -3px 0 0 #f1c40f");  // bottom
        if (!set.has(`${r},${c - 1}`)) shadows.push("inset  3px 0 0 0 #f1c40f");  // left
        if (!set.has(`${r},${c + 1}`)) shadows.push("inset -3px 0 0 0 #f1c40f");  // right

        cell.style.boxShadow = shadows.join(", ");
    }
}

/**
 * Highlights the cell the computer attacked last turn with an orange outline.
 * Removes the highlight from the previously marked cell first.
 *
 * @param {number} row - Zero-based row index of the computer's move.
 * @param {number} col - Zero-based column index of the computer's move.
 */
function highlightLastComputerMove(row, col) {
    const prev = document.querySelector(".last-computer-move");
    if (prev) prev.classList.remove("last-computer-move");
    const table = document.getElementById("human-board");
    table.rows[row + 1].cells[col + 1].classList.add("last-computer-move");
}

async function loadVersion() {
    const response = await fetch("api/version");
    const version = await response.text();
    document.getElementById("version").innerText = "Version: " + version;
}

// On page load: start a new game, build both boards, and show the player's ships
window.onload = async function () {
    const response = await fetch("api/battleship/start-game", { method: "POST" });
    const guessesLeft = await response.json();
    document.getElementById("guesses-left").innerText = `Guesses left: ${guessesLeft}`;

    // Build both boards; computer-board is clickable, human-board is not
    loadBoard("computer-board", true);
    loadBoard("human-board", false);

    // Fetch humanStatus so the player's ships show up before the first attack
    const statusResponse = await fetch("api/battleship/humanStatus");
    const humanDTO = await statusResponse.json();
    renderHumanBoard(humanDTO.homeGrid);
    await loadVersion();
};