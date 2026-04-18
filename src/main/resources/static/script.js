const USER_FEEDBACK_LINK = 'https://docs.google.com/forms/d/e/1FAIpQLSdr2xzx1jbwUW6hDL321aXq4rXhb8n56_qPCpv8hvU4RCcTCA/viewform';
function handleFeedbackClick() {
    window.open(USER_FEEDBACK_LINK, '_blank');
}
const size = 10;
const letters = "ABCDEFGHIJ";
let gameOver= false;

// Function to load the board
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
        labelCell.textContent = i + 1;
        labelCell.className = "label";
        row.appendChild(labelCell);
        for (let j = 0; j < size; j++) {
            const cell = row.insertCell();
            cell.className = "water";
            cell.textContent = "~";
            if (clickable) {
                cell.onclick = function () {
                    submitAttack(i, j);
                };
            }
        }
    }
}

//SUBMIT ATTACK
async function submitAttack(row, col) {
    if (gameOver) return;

    const response = await fetch("api/battleship/attack", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ row: row, column: col })
    });

    const data = await response.json();

    if (data.isError) {
        document.getElementById("message").innerText = "Cell already attacked.";
        return;
    }

    updateComputerBoardCell(row, col, data.grid[row][col]);

    /* Repaint the whole human-board and highlight the computer's move */
    renderHumanBoard(data.homeGrid);
    if (data.computerRow >= 0) {
        highlightLastComputerMove(data.computerRow, data.computerCol);
    }

    document.getElementById("guesses-left").innerText = `Guesses left: ${data.guessesLeft}`;

    /* Show both player and computer messages, handle WIN/LOSS */
    document.getElementById("message").innerText = data.message;
    document.getElementById("computer-message").innerText = data.computerMessage || "";

    if (data.gameStatus !== "IN_PROGRESS") {
        gameOver = true;
        if (data.gameStatus === "WIN") {
            document.getElementById("message").innerText = "You Won!";
        } else {
            document.getElementById("message").innerText = "Computer Wins!";
        }
    }
}

//UPDATE COMPUTER'S BOARD
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

//UPDATE HUMAN'S BOARD
function renderHumanBoard(homeGrid) {
    const table = document.getElementById("human-board");
    for (let r = 0; r < homeGrid.length; r++) {
        for (let c = 0; c < homeGrid[r].length; c++) {
            const cell = table.rows[r + 1].cells[c + 1];
            const state = homeGrid[r][c].toLowerCase();
            cell.className = state;
            if (state === "hit")       cell.textContent = "X";
            else if (state === "miss") cell.textContent = "O";
            else if (state === "ship") cell.textContent = "#";
            else                       cell.textContent = "~";
        }
    }
}

//HIGHLIGHT COMPUTER'S MOVE
function highlightLastComputerMove(row, col) {
    const prev = document.querySelector(".last-computer-move");
    if (prev) prev.classList.remove("last-computer-move");
    const table = document.getElementById("human-board");
    table.rows[row + 1].cells[col + 1].classList.add("last-computer-move");
}

async function loadVersion() {
    const response = await fetch("api/version");
    const version = await response.text(); //https://stackoverflow.com/questions/41946457/getting-text-from-fetch-response-object
    document.getElementById("version").innerText = "Version: " + version;
}

//LOAD BOARD
window.onload = async function () {
    const response = await fetch("api/battleship/start-game", { method: "POST" });
    const guessesLeft = await response.json();
    document.getElementById("guesses-left").innerText = `Guesses left: ${guessesLeft}`;

    /* Build both boards, computer-board is clickable, human-board is not */
    loadBoard("computer-board", true);
    loadBoard("human-board", false);

    /* Fetch humanStatus so the player's ships show up before the first attack */
    const statusResponse = await fetch("api/battleship/humanStatus");
    const humanDTO = await statusResponse.json();
    renderHumanBoard(humanDTO.homeGrid);
    await loadVersion();
};