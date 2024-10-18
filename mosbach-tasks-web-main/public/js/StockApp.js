const APIKEY = 'Vf080TfqbqvnJHcpt2aP9Ec1XL21Xb0D'; // Externer API-Schlüssel

///* Data display *//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function displayAllDatabaseData(){
    displayUserBudget(); 
    displayTotalPortfolioValue();
    getAllTransactions();
    getAllPortfolioStocks();
}


///*Cookies*//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//Get a cookie
function getCookie(cookieName){
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

//Set a cookie
function setCookie(cookieName, cookieValue) {
    document.cookie = cookieName+"=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; SameSite=Strict";
    document.cookie = cookieName + "=" + cookieValue + "; SameSite=Strict ; path=/";
}

///*Auth*//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


//Get the Profile for an user of the Website
function login(profileSchema){
    event.preventDefault();
    let email = profileSchema.email.value;
    let password = profileSchema.password.value;
    const settingsLogin = {
        "async": true, 
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
            setCookie("token", data.token);
            setCookie("firstname", data.user.firstname);
            setCookie("lastname", data.user.lastname);
            setCookie("email", data.user.email);
            setCookie("budget", roundToTwoDecimalPlaces(data.user.budget));
            setCookie("password", data.user.password);
            document.location="home.html";
            
        },
        "error": function(xhr) {
            if (xhr.status === 401 || xhr.status === 404) {
                alert(JSON.parse(xhr.responseText).answer);
            } else{
                alert("An unexpected error occurred. Status: " + xhr.status);
            }
        }
    };

    $.ajax(settingsLogin);
}

// Create a new user
function register(profileSchema){
    event.preventDefault();
    const settingsRegister = {
        "async": true, 
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

///*Round functions*//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function roundDownToTwoDecimalPlaces(num) {
    return Math.floor(num * 100) / 100;
}

function roundToTwoDecimalPlaces(value) {
    return Math.round(value * 100) / 100;
}

///*Create orders*//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//Get todays date
function getCurrentDateTime() {
    const now = new Date(); // Aktuelles Datum und Uhrzeit

    // Formatieren der einzelnen Teile
    const year = now.getFullYear(); // Jahr
    const month = String(now.getMonth() + 1).padStart(2, '0'); // Monat 
    const day = String(now.getDate()).padStart(2, '0'); // Tag
    const hours = String(now.getHours()).padStart(2, '0'); // Stunden
    const minutes = String(now.getMinutes()).padStart(2, '0'); // Minuten
    const seconds = String(now.getSeconds()).padStart(2, '0'); // Sekunden

    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
}

//Create a buy Order
function buyStock(){
    event.preventDefault();
    let stockAmount;
    const priceDisplay = document.getElementById("price-display").textContent.trim();
    const price = parseFloat(priceDisplay.replace('$', '').trim());
    console.log(price);

    if (isNaN(price)) {
        alert("Enter a valid stock!");
        return;
    }
    if ($("#quantity-label").text() === "Quantity in $:") {
        const quantity = parseFloat($('#quantity').val());
        console.log(quantity);
        if (isNaN(quantity) || quantity <= 0) {
            alert("Enter a valid quantity in $!");
            return;
        }
        stockAmount = quantity / price;
    } else {
        stockAmount = parseFloat($('#quantity').val());
        if (isNaN(stockAmount) || stockAmount <= 0) {
            alert("Enter a valid stock amount!");
            return;
        }
    }
    const settingsBuyStock = {
        "async": true,
        "url": "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/order/buy",
        "method": "POST",
        "headers": {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        "data": JSON.stringify({
            "token": getCookie("token"),
            "transactioncontent": {
                "transactiontype": 1,
                "stockamount": stockAmount,
                "date": getCurrentDateTime(),
                "priceperstock": price,
                "totalprice": roundToTwoDecimalPlaces(price * stockAmount),
                "email": getCookie("email"),
                "symbol": $('#stock-name').val()
            }
        })
        ,
        "success": function(data) {
            console.log(data);
            alert(data.answer);
            console.log(getCookie("budget"));
            setCookie("budget", roundToTwoDecimalPlaces(getCookie("budget") - roundToTwoDecimalPlaces(price * stockAmount)));
            console.log(getCookie("budget"));
            displayAllDatabaseData();
            $('#quantity').val("");
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

//Create a sell order
function sellStock(){
    event.preventDefault();
    let stockAmount;
    const priceDisplay = document.getElementById("price-display").textContent.trim();
    const price = parseFloat(priceDisplay.replace('$', '').trim());
    console.log(price);

    if (isNaN(price)) {
        alert("Enter a valid stock!");
        return;
    }
    if ($("#quantity-label").text() === "Quantity in $:") {
        const quantity = parseFloat($('#quantity').val());
        console.log(quantity);
        if (isNaN(quantity) || quantity <= 0) {
            alert("Enter a valid quantity in $!");
            return;
        }
        stockAmount = quantity / price;
    } else {
        stockAmount = parseFloat($('#quantity').val());
        if (isNaN(stockAmount) || stockAmount <= 0) {
            alert("Enter a valid stock amount!");
            return;
        }
    }
    const settingsSellStock = {
        "async": true,
        "url": "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/order/sell",
        "method": "POST",
        "headers": {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        "data": JSON.stringify({
            "token": getCookie("token"),
            "transactioncontent": {
                "transactiontype": 0,
                "stockamount": stockAmount,
                "date": getCurrentDateTime(),
                "priceperstock": price,
                "totalprice": roundToTwoDecimalPlaces(price * stockAmount),
                "email": getCookie("email"),
                "symbol": $('#stock-name').val()
            }
        })
        ,
        "success": function(data) {
            console.log(data);
            alert(data.answer);
            setCookie("budget", roundToTwoDecimalPlaces(getCookie("budget") + roundToTwoDecimalPlaces(price * stockAmount)));
            displayAllDatabaseData();
            $('#quantity').val("");
        },
        "error": function(xhr) {
            if (xhr.status === 401 || xhr.status === 500 || xhr.status === 404 || xhr.status === 400) {
                alert(JSON.parse(xhr.responseText).answer);
            } else{
                alert("Es ist ein unbekannter Fehler aufgetreten. Status: " + xhr.status);
            }
        }
    };
    $.ajax(settingsSellStock);
}

// Change Label text for buy quantity
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

// Check if all necessary fields for creating an order are filled
function checkFields() {
    const stockName = document.getElementById('stock-name').value.trim();
    const quantity = document.getElementById('quantity').value.trim();

    const buyButton = document.getElementById('buy-stock');
    const sellButton = document.getElementById('sell-stock');

    if (stockName !== "" && quantity !== "") {
        buyButton.disabled = false; 
        sellButton.disabled = false; 
    } else {
        buyButton.disabled = true;
        sellButton.disabled = true;
    }
}

///*Edit/Reset/Delete profile*//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//Display current profile data
function setProfileValues(){
    document.getElementById("first-name").value = getCookie("firstname");
    document.getElementById("last-name").value = getCookie("lastname");
    document.getElementById("email").value = getCookie("email");
}

//Edit an user
function editUser(){
    event.preventDefault();
    let budgetValue = $('#amount-selection').val(); // Entfernt Leerzeichen am Anfang und Ende

    // Überprüfen, ob der Wert numerisch ist und nicht leer, sonst auf 0 setzen
    let budget = (budgetValue !== '' && !isNaN(parseFloat(budgetValue))) ? parseFloat(budgetValue) : 0;

    
    const editRegister = {
        "async": true,
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
                "firstname": $('#first-name').val(),
                "lastname": $('#last-name').val(),
                "email": $('#email').val(),
                "budget": budget
            }
        }),
        "success": function(data) {
            console.log("Antwort vom Server:", data);
            setCookie("firstname", data.firstname);
            setCookie("lastname", data.lastname);
            setCookie("email", data.email);
            setCookie("budget", roundToTwoDecimalPlaces(data.budget));
            alert("User edited successfully");
        }
        ,
        "error": function(xhr) {
            console.log(xhr);
            if (xhr.status === 401) {
                alert(JSON.parse(xhr.responseText).answer);
            } else {
                alert("Es ist ein unbekannter Fehler aufgetreten. Status: " + xhr.status);
            }
        }
    };

    $.ajax(editRegister);
}

// Reset an user
function resetProfile(){
    event.preventDefault();
    const resetRegister = {
        "async": true, 
        "url": "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/user/reset",
        "method": "PUT",
        "headers": {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        "data": JSON.stringify({
            "token": getCookie("token"),
            "email": getCookie("email"),
        }),
        "success": function(data) {
            const confirmation = confirm("Are you sure you want to reset your profile?");
            if (confirmation) {
                alert("Profile successfully resetted!.");
            setCookie("budget", data.budget);
            } 
        },
        "error": function(xhr) {
            if (xhr.status === 401 || xhr.status === 400) {
                alert(JSON.parse(xhr.responseText).answer);
            } else{
                alert("Es ist ein unbekannter Fehler aufgetreten. Status: " + xhr.status);
            }
        }
    };

    $.ajax(resetRegister);
}

//Delete an user
function deleteProfile(){
    event.preventDefault();
    const deleteRegister = {
        "async": true, 
        "url": "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/user",
        "method": "DELETE",
        "headers": {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        "data": JSON.stringify({
            "token": getCookie("token"),
            "email": getCookie("email"),
        }),
        "success": function(data) {
            const confirmation = confirm("Are you sure you want to delete your profile?");
            if (confirmation) {
                alert(data.answer);
                document.location="vorHome.html";
            } 
        },
        "error": function(xhr) {
            if (xhr.status === 401 || xhr.status === 400) {
                alert(JSON.parse(xhr.responseText).answer);
            } else{
                alert("Es ist ein unbekannter Fehler aufgetreten. Status: " + xhr.status);
            }
        }
    };

    $.ajax(deleteRegister);
}

///*Get and display stock price*//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//Display the stock price in the frontend 
function displayStockPrice(value){
    $('#price-display').text(value);
}

//Fetch a stock price of database or external API
function fetchStockPrice(){
    let stockName = getStockName();
    getStockPriceFromDB(stockName)
        .done(function(data) {
            displayStockPrice(data.stockprice);
        })
        .fail(function(jqXHR, textStatus, errorThrown) {
            if (jqXHR.status === 500) {
                displayStockPrice('Error retrieving data. Please try again later.');
            } else {
                getStockPriceFromAPI(stockName)
                    .done(function(data) {
                        if (data.status === 'OK' && data.results && data.results.length > 0) {
                            const closeValue = parseFloat(data.results[0].c); 
                            const roundedCloseValue = closeValue.toFixed(2);
                            insertNewStock(stockName, roundedCloseValue);
                            displayStockPrice(roundedCloseValue);
                        } else if (!data.results) {
                            displayStockPrice('Stock not found or no data available.');
                        } else if (data.results.length === 0) {
                            displayStockPrice('No closing price data available.');
                        } else {
                            displayStockPrice('Unknown error retrieving data.');
                        }
                    })
                    .fail(function(jqXHR, textStatus, errorThrown) {
                        if (jqXHR.status === 429) {
                            displayStockPrice('Too many requests. Please try again later.');
                        } else {
                            console.error('Error:', textStatus, errorThrown);
                            displayStockPrice('Error retrieving data. Please try again later.');
                        }
                    });            
                    }
        });
}

// Get stock price from database
function getStockPriceFromDB(stockName){
    const getStockPriceDBRegister = {
        "async": true,
        "url": "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/stock?email=" + getCookie("email") + "&token=" + getCookie("token") + "&symbol=" + stockName,
        "method": "GET",
        "headers": {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    };

    return $.ajax(getStockPriceDBRegister);
}

//Get stock name
function getStockName() {
    let stockNameLabel = document.getElementById("stock-name");
    return stockNameLabel.value;

}

//Get stock price from the external API
function getStockPriceFromAPI(stockName) {
    const getStockPriceAPIRegister = {
        "async": true, 
        "url": `https://api.polygon.io/v2/aggs/ticker/${stockName}/prev?adjusted=true&apiKey=Vf080TfqbqvnJHcpt2aP9Ec1XL21Xb0D`,
        "method": "GET",
        "dataType": 'json',
        };
    return $.ajax(getStockPriceAPIRegister);
}

//Insert a stock to the database
function insertNewStock(stockName, stockPrice){
    const settingsInsertStock = {
        "async": true, 
        "url": "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/stock",
        "method": "POST",
        "headers": {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        "data": JSON.stringify({
            "tokenemail": {
                "token": getCookie("token"),
                "email": getCookie("email")
            },
            "stock": {
                "symbol": stockName,
                "stockprice": stockPrice,
                "name": "-"
            }
        })
    };

    $.ajax(settingsInsertStock);
}

//Create event for displaying the stock price
function showStockPriceViaEvent() {
    const inputField = document.getElementById('stock-name');
    inputField.addEventListener('keypress', handleInputKeypress);
    inputField.addEventListener('blur', fetchStockPrice);
}

//Handle enter keypress for displaying the stock price
function handleInputKeypress(e) {
    if (e.key === 'Enter') { 
        fetchStockPrice();
    }
}

///*Get and display all portfolio data*//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//Get all transactions of an user
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
        displayTransactionHistory(transactions);
    });
}

//Display all transactions of an user
function displayTransactionHistory(transactions) {
    const transactionHistoryContainer = document.querySelector('.transaction-history');
    transactionHistoryContainer.innerHTML = ''; 
    let type;
    const heading = document.createElement('h2');
    heading.textContent = 'Transaction History';
    transactionHistoryContainer.appendChild(heading);

    transactions.forEach(transaction => {
        console.log(transaction);
        const transactionDiv = document.createElement('div');
        if(transaction.transactiontype === 1){
            type = "Bought"
        }
        else{
            type = "Sold"
        }
        transactionDiv.textContent = `${type} ${transaction.stockamount} ${transaction.symbol} at price of ${transaction.priceperstock}$ for ${transaction.totalprice}$`;
        transactionHistoryContainer.appendChild(transactionDiv);
    });
}

//Get all stocks in portfolio of an user
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
        displayPortfolioStocks(portfolioStocks)
    });
}

//Display alle stocks in portfolio of an user
function displayPortfolioStocks(portfolioStocks) {
    const stockListContainer = document.querySelector('.portfolio .stock-list');
    stockListContainer.innerHTML = ''; 
    let totalCurrentPortfolioValue = 0; 
    let totalBoughtPortfolioValue = 0; 

    portfolioStocks.forEach(stock => {
        totalCurrentPortfolioValue += stock.currentvalue;
        totalBoughtPortfolioValue += stock.boughtvalue;
        const stockDiv = document.createElement('div');
        const stockValue = roundToTwoDecimalPlaces(parseFloat(stock.currentvalue));

        const { percentageChange, changeClass } = calculatePercentage(stock.boughtvalue, stock.currentvalue);

        stockDiv.innerHTML = `${stock.symbol}: ${stockValue}$ <span class="change ${changeClass}">${percentageChange}%</span>`;
        stockListContainer.appendChild(stockDiv);
    });
}

//Calculate percentage change of portfolio elements
function calculatePercentage(boughtvalue, currentvalue) {
    const percentageChange = ((currentvalue - boughtvalue) / boughtvalue * 100).toFixed(2);

    const changeClass = percentageChange >= 0 ? 'positive' : 'negative';
    const sign = percentageChange >= 0 ? '+' : '';

    return {
        percentageChange: `${sign}${percentageChange}%`,
        changeClass: changeClass
    };
}

//Display the remaining user budget
function displayUserBudget(){
    event.preventDefault();
    const settingsGetBudget = {
        "async": false,
        "url": "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/user?email=" + getCookie("email") + "&token=" + getCookie("token"),
        "method": "GET",
        "headers": {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        "success": function(data) {
            $(".remaining-budget").text(data.budget + " $");
            setCookie("budget", roundToTwoDecimalPlaces(data.budget));
        },
        "error": function(xhr) {
            if (xhr.status === 401 || xhr.status === 400) {
                $(".remaining-budget").text(JSON.parse(xhr.responseText).answer);
            } else{
                $(".remaining-budget").text("An unexpected error occured");
                
            }
        }
    }
    $.ajax(settingsGetBudget);
}

//Display the total portfolio value
function displayTotalPortfolioValue(){
    event.preventDefault();
    const settingsGetPortfolioValue = {
        "async": false,
        "url": "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/portfolio?email=" + getCookie("email") + "&token=" + getCookie("token"),
        "method": "GET",
        "headers": {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        "success": function(data) {
            console.log(data);
            $(".portfolio-value").text(data.value  + " $");
        },
        "error": function(xhr) {
            if (xhr.status === 401 || xhr.status === 400) {
                $(".portfolio-value").text(JSON.parse(xhr.responseText).answer);
            } else{
                $(".portfolio-value").text("An unexpected error occured");
                
            }
        }
    }
    $.ajax(settingsGetPortfolioValue);
}
