const APIKEY = 'Vf080TfqbqvnJHcpt2aP9Ec1XL21Xb0D'; // Externer API-Schlüssel

///* Login *//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////




///*Cookies*//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//Getting cookie
function getCookie(cookieName){
    // source W3Schools
    let name = cookieName + "=";
    let decodedCookie = decodeURIComponent(document.cookie);
    let ca = decodedCookie.split(';');
    for(const element of ca) {
        let c = element;
        while (c.charAt(0) == ' ') {
        c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
        return c.substring(name.length, c.length);
        }
    }
    return "";
}

//Setting cookie
function setCookie(cookieName, cookieValue) {
    document.cookie = cookieName+"=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; SameSite=Strict";
    document.cookie = cookieName + "=" + cookieValue + "; SameSite=Strict ; path=/";
}

//Testing if user cookie is there
function testCookie(){
    if(getCookie("userID") != "" && getCookie("userID") != null){}
    else{
        document.location = "login.html";
    }
}

// GET USER ID FROM COOKIE --------------------------------------------------------------------------------------------------------------------------------------------------------
//getting Profile for user of the Website
function login(profileSchema){
    event.preventDefault();
    let email = profileSchema.email.value;
    let password = profileSchema.password.value;
    const settingsLogin = {
        "async": true, // Asynchrone Anfrage
        "url": "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/auth",
        "method": "POST",
        "headers": {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        "data": JSON.stringify({
            "email": email,
            "password": password
        }),
        "success": function(data) {
            // Erfolgreicher Aufruf
            console.log(data)
            setCookie("token", data.token);
            setCookie("firstname", data.User.firstname);
            setCookie("lastname", data.User.lastname);
            setCookie("email", data.User.email);
            setCookie("budget", data.User.budget);
            setCookie("password", data.User.password);
            // localStorage.setItem("token", data.token);
            // localStorage.setItem("firstname", data.User.firstname);
            // localStorage.setItem("lastname", data.User.lastname);
            // localStorage.setItem("email", data.User.email);
            // console.log("Werte gespeichert:", localStorage.getItem("firstname"), localStorage.getItem("lastname"), localStorage.getItem("email"));
            // //setCookie("password", data.User.password); // Passwort nicht speichern
            document.location="home.html";
            // console.log("Werte gespeichert:", localStorage.getItem("firstname"), localStorage.getItem("lastname"), localStorage.getItem("email"));
            
        },
        "error": function(xhr) {
            // Fehlerbehandlung je nach Statuscode
            if (xhr.status === 401 || xhr.status === 404) {
                alert(JSON.parse(xhr.responseText).answer);
            } else{
                alert("An unexpected error occurred. Status: " + xhr.status);
            }
        }
    };

    $.ajax(settingsLogin);
}

function register(profileSchema){
    event.preventDefault();
    const settingsRegister = {
        "async": true, // Asynchrone Anfrage
        "url": "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/user",
        "method": "POST",
        "headers": {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        "data": JSON.stringify({
            "firstname": profileSchema.firstname.value,
            "lastname": profileSchema.lastname.value,
            "email": profileSchema.email.value,
            "password": profileSchema.password.value,
            "budget": profileSchema.budget.value
        }),
        "success": function(data) {
            console.log(data.answer);
            alert("Erfolgreich registriert! Sie können sich nun einloggen.");
        },
        "error": function(xhr) {
            console.log(xhr);
            if (xhr.status === 401 || xhr.status === 404) {
                alert(JSON.parse(xhr.responseText).answer);
                console.log(xhr);
            } else{
                alert("Es ist ein unbekannter Fehler aufgetreten. Status: " + xhr.status);
            }
        }
    };
    $.ajax(settingsRegister);
}

function checkIfPriceIsDisplayed(){
    const priceDisplay = document.getElementById("price-display").textContent.trim();
    
    // Überprüfen, ob der Inhalt eine gültige Zahl ist
    const price = parseFloat(priceDisplay);

    // Prüfen, ob price eine Zahl ist
    if (isNaN(price)) {
        alert("Es ist kein gültiger Preis angegeben.");
    } else {
        alert("Es ist kein gültiger Preis angegeben.");
    }
}

function getCurrentDateTime() {
    const now = new Date(); // Aktuelles Datum und Uhrzeit

    // Formatieren der einzelnen Teile
    const year = now.getFullYear(); // Jahr
    const month = String(now.getMonth() + 1).padStart(2, '0'); // Monat (0-basiert, also +1 und 2-stellig)
    const day = String(now.getDate()).padStart(2, '0'); // Tag
    const hours = String(now.getHours()).padStart(2, '0'); // Stunden
    const minutes = String(now.getMinutes()).padStart(2, '0'); // Minuten
    const seconds = String(now.getSeconds()).padStart(2, '0'); // Sekunden

    // Zusammensetzen im gewünschten Format
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
}

function roundDownToTwoDecimalPlaces(num) {
    return Math.floor(num * 100) / 100;
}

function roundToTwoDecimalPlaces(value) {
    return Math.round(value * 100) / 100;
}

function buyStock(){
    event.preventDefault();
    let stockAmount;
    const priceDisplay = document.getElementById("price-display").textContent.trim();
    // Entferne das Dollarzeichen und parse den Preis
    const price = parseFloat(priceDisplay.replace('$', '').trim());
    console.log(price);

    if (isNaN(price)) {
        alert("Enter a valid stock!");
        return;
    }
    if ($("#quantity-label").text() === "Quantity in $:") {
        // Hier ist die Division
        const quantity = parseFloat($('#quantity').val());
        console.log(quantity);
        if (isNaN(quantity) || quantity <= 0) {
            alert("Enter a valid quantity in $!");
            return;
        }
        stockAmount = roundDownToTwoDecimalPlaces(quantity / price); // Berechnung für die Menge
    } else {
        stockAmount = parseFloat($('#quantity').val());
        if (isNaN(stockAmount) || stockAmount <= 0) {
            alert("Enter a valid stock amount!");
            return;
        }
    }
    const settingsBuyStock = {
        "async": true, // Asynchrone Anfrage
        "url": "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/order/buy",
        "method": "POST",
        "headers": {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        "data": JSON.stringify({
            "token": getCookie("token"),
            "transactionContent": {
                "transactionType": 1,
                "stockAmount": stockAmount,
                "date": getCurrentDateTime(),
                "pricePerStock": price,
                "totalPrice": roundToTwoDecimalPlaces(price * stockAmount),
                "email": getCookie("email"),
                "symbol": $('#stock-name').val()
            }
        })
        ,
        "success": function(data) {
            console.log(data);
            alert(data.answer);
            loadPortfolioDataFromDatabase();
        },
        "error": function(xhr) {
            if (xhr.status === 401 || xhr.status === 400) {
                alert(JSON.parse(xhr.responseText).answer);
            } else{
                alert("Es ist ein unbekannter Fehler aufgetreten. Status: " + xhr.status);
            }
        }
    };
    $.ajax(settingsBuyStock);
}
    
// Funktion, um das Label und den Button zu toggeln (Portfolio-Seite)
function toggleLabel() {
    var label = document.getElementById('quantity-label');
    var button = document.getElementById('toggle-label');

    if (label.textContent === 'Quantity in $:') {
        label.textContent = 'Quantity in Stocks:';
        button.textContent = 'Switch to $';
    } else {
        label.textContent = 'Quantity in $:';
        button.textContent = 'Switch to Stocks';
    }
}


function editUser(){
    event.preventDefault();
    const editRegister = {
        "async": true, // Asynchrone Anfrage
        "url": "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/user",
        "method": "PUT",
        "headers": {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        "data": JSON.stringify({
            "token": getCookie("token"),
            "currentmail": getCookie("email"),
            "user": {
            "firstname": $('#first-name').value,
            "lastname": $('#last-name').value,
            "email": $('#email').value,
            "password": getCookie("password"),
            "budget": getCookie("budget")
        }}),
        "success": function(data) {
            alert("Erfolgreich geändert!.");
        },
        "error": function(xhr) {
            console.log(xhr);
            if (xhr.status === 401) {
                alert("Sie sind nciht berechtigt das Profil zu ändern!.");
            }
            else{
                alert("Es ist ein unbekannter Fehler aufgetreten. Status: " + xhr.status);
            }
        }
    };

    $.ajax(editRegister);
}
//////////////////////////////////////////// Aktienpreis //////////////////////////////////////////

//Funktion, um den eingegebenen Aktiennamen innerhalb des Portfolios zu bekommen
function getStockName() {
    let stockNameLabel = document.getElementById("stock-name");
    //console.log(stockNameLabel.value);
    return stockNameLabel.value;

}

//Funktion, um den aktuellen Preis der eingegebenen Aktie zu bekommen 
function getStockPrice() {
    let stockName = getStockName(); // Replace this with the input field name for the stock symbol
    let url = `https://api.polygon.io/v2/aggs/ticker/${stockName}/prev?adjusted=true&apiKey=Vf080TfqbqvnJHcpt2aP9Ec1XL21Xb0D`;

    $.ajax({
        url: url,
        method: 'GET',
        dataType: 'json',
        success: function(data) {
            console.log(data); // Debugging: Check the data structure

            if (data.status === 'OK' && data.results && data.results.length > 0) {
                const closeValue = parseFloat(data.results[0].c); // The closing price
                const roundedCloseValue = closeValue.toFixed(2);
                $('#price-display').text(`${roundedCloseValue}$`);
            } else if (!data.results) {
                $('#price-display').text('Stock not found or no data available.');
            } else if (data.results.length === 0) {
                $('#price-display').text('No closing price data available.');
            } else {
                $('#price-display').text('Unknown error retrieving data.');
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            if (jqXHR.status === 429) {
                $('#price-display').text('Too many requests. Please try again later.');
            } else {
                console.error('Error:', textStatus, errorThrown);
                $('#price-display').text('Error retrieving data.');
            }
        }
    });
}

//Funktion, um die Events zur Preisanzeige zu implementieren
function showStockPriceViaEvent() {
    const inputField = document.getElementById('stock-name');
    inputField.addEventListener('keypress', handleInputKeypress);
    inputField.addEventListener('blur', getStockPrice);
}

//Funktion, um eine Enter-Taste Eingabe zu empfangen
function handleInputKeypress(e) {
    if (e.key === 'Enter') { 
        getStockPrice();
    }
}

function setProfileValues(){
    // console.log("Vor dem Abrufen der Werte:");
    // console.log("Firstname:", localStorage.getItem("firstname"));
    // console.log("Lastname:", localStorage.getItem("lastname"));
    // console.log("Email:", localStorage.getItem("email"));

    // console.log(getCookie("firstName"));
    // console.log(document.cookie)
    // Setze den Wert des Vorname-Feldes
    document.getElementById("first-name").value = getCookie("firstname");
    // Setze den Wert des Nachname-Feldes
    document.getElementById("last-name").value = getCookie("lastname");
    // Setze den Wert des E-Mail-Feldes
    document.getElementById("email").value = getCookie("email");
    // document.getElementById("first-name").value = localStorage.getItem("firstname");
    // // Setze den Wert des Nachname-Feldes
    // document.getElementById("last-name").value = localStorage.getItem("lastname");
    // // Setze den Wert des E-Mail-Feldes
    // document.getElementById("email").value = localStorage.getItem("email");
}

function checkFields() {
    const stockName = document.getElementById('stock-name').value.trim();
    const quantity = document.getElementById('quantity').value.trim();

    const buyButton = document.getElementById('buy-stock');
    const sellButton = document.getElementById('sell-stock');

    // Überprüfen, ob beide Felder ausgefüllt sind
    if (stockName !== "" && quantity !== "") {
        buyButton.disabled = false; // Button aktivieren
        sellButton.disabled = false; // Button aktivieren
    } else {
        buyButton.disabled = true; // Button deaktivieren
        sellButton.disabled = true; // Button deaktivieren
    }
}


////////////////////////////////// Portfolio //////////////////////////////////////////////
function loadPortfolioDataFromDatabase(){
    getAllTransactions();
    getAllPortfolioStocks();
}


function getAllTransactions(){
    event.preventDefault();
    const settingsGetAllTransactions = {
        "async": false,
        "url": "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/transactions?email=" + getCookie("email") + "&token=" + getCookie("token") + "&sortby=date",
        "method": "GET",
        "headers": {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    }
    $.ajax(settingsGetAllTransactions).done(function (transactions) {
        console.log(transactions);
        displayTransactionHistory(transactions);
    });
}

function displayTransactionHistory(transactions) {
    const transactionHistoryContainer = document.querySelector('.transaction-history');
    transactionHistoryContainer.innerHTML = ''; // Vorherige Inhalte entfernen
    let type;
    const heading = document.createElement('h2');
    heading.textContent = 'Transaction History';
    transactionHistoryContainer.appendChild(heading);

    transactions.forEach(transaction => {
        console.log(transaction);
        const transactionDiv = document.createElement('div');
        if(transaction.transactionType === 1){
            type = "Bought"
        }
        else{
            type = "Sold"
        }
        transactionDiv.textContent = `${type} ${transaction.stockAmount} ${transaction.symbol} at price of ${transaction.pricePerStock}$ for ${transaction.totalPrice}$`;
        transactionHistoryContainer.appendChild(transactionDiv);
    });
}

function getAllPortfolioStocks(){
    event.preventDefault();
    const settingsGetAllPortfolioStocks = {
        "async": false,
        "url": "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/portfolioStocks?email=" + getCookie("email") + "&token=" + getCookie("token") + "&sortby=symbol",
        "method": "GET",
        "headers": {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    }
    $.ajax(settingsGetAllPortfolioStocks).done(function (portfolioStocks) {
        console.log(portfolioStocks);
        displayPortfolioStocks(portfolioStocks)
        //localStorage.setItem("portfolioStockArray", JSON.stringify(portfolioStocks));
    });
}

function displayPortfolioStocks(portfolioStocks) {
    const stockListContainer = document.querySelector('.portfolio .stock-list');
    stockListContainer.innerHTML = ''; // Vorherige Inhalte entfernen
    let totalCurrentPortfolioValue = 0; // Initialisierung auf 0
    let totalBoughtPortfolioValue = 0; // Initialisierung auf 0

    portfolioStocks.forEach(stock => {
        totalCurrentPortfolioValue += stock.currentValue;
        totalBoughtPortfolioValue += stock.boughtValue;
        const stockDiv = document.createElement('div');
        const stockValue = roundToTwoDecimalPlaces(parseFloat(stock.currentValue));

        // Berechne die prozentuale Änderung mit der neuen Methode
        const { percentageChange, changeClass } = calculatePercentage(stock.boughtValue, stock.currentValue);

        // Füge die Daten in das Div-Element ein
        stockDiv.innerHTML = `${stock.symbol}: ${stockValue}$ <span class="change ${changeClass}">${percentageChange}%</span>`;
        stockListContainer.appendChild(stockDiv);
    });

    // Gesamtwerte anzeigen
    displayTotalPortfolioValues(totalCurrentPortfolioValue, totalBoughtPortfolioValue);
}



function calculatePercentage(boughtValue, currentValue) {
    // Berechne die prozentuale Änderung
    const percentageChange = ((currentValue - boughtValue) / boughtValue * 100).toFixed(2);

    // Überprüfe, ob der Wert positiv oder negativ ist, und lege die CSS-Klasse fest
    const changeClass = percentageChange >= 0 ? 'positive' : 'negative';
    const sign = percentageChange >= 0 ? '+' : '';

    // Gib das Ergebnis als Objekt zurück, um sowohl den Wert als auch die CSS-Klasse zu nutzen
    return {
        percentageChange: `${sign}${percentageChange}%`, // Prozentwert mit Vorzeichen
        changeClass: changeClass // CSS-Klasse für positive oder negative Veränderung
    };
}

function displayTotalPortfolioValues(totalCurrentPortfolioValue, totalBoughtPortfolioValue) {
    console.log(totalCurrentPortfolioValue);
    console.log(totalBoughtPortfolioValue);
    const portfolioValueContainer = document.querySelector('.portfolio .portfolio-value');

    // Berechnung der prozentualen Veränderung
    const percentageChange = roundToTwoDecimalPlaces(((totalCurrentPortfolioValue - totalBoughtPortfolioValue) / totalBoughtPortfolioValue * 100));
    
    // Überprüfen, ob die Veränderung positiv oder negativ ist
    const changeClass = percentageChange >= 0 ? 'positive' : 'negative';
    const sign = percentageChange >= 0 ? '+' : '';

    // Rundung des aktuellen Portfolio-Werts auf zwei Dezimalstellen
    const roundedCurrentValue = roundToTwoDecimalPlaces(totalCurrentPortfolioValue);

    // Aktualisierung des HTML-Codes
    portfolioValueContainer.innerHTML = `${roundedCurrentValue} $ <span class="percentage ${changeClass}">${sign}${percentageChange}%</span>`;
}
