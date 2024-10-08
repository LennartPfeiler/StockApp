﻿const APIKEY = 'Vf080TfqbqvnJHcpt2aP9Ec1XL21Xb0D'; // Externer API-Schlüssel

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
            let message;
            if (xhr.status === 401) {
                alert("Email oder Passwort ist falsch!");
            } else if (xhr.status === 500) {
                alert("Bitte registrieren Sie sich erst!")
            }
            else{
                alert("Es ist ein unbekannter Fehler aufgetreten. Status: " + xhr.status);
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
            console.log(data);
            alert("Erfolgreich registriert! Sie können sich nun einloggen.");
        },
        "error": function(xhr) {
            console.log(xhr);
            if (xhr.status === 401) {
                alert("Sie sind bereits registriert! Bitte loggen Sie sich ein.");
            }
            else{
                alert("Es ist ein unbekannter Fehler aufgetreten. Status: " + xhr.status);
            }
        }
    };
    $.ajax(settingsRegister);
}

// Funktion, um den aktuellen Preis anzuzeigen (Portfolio-Seite)
function showPrice() {
    var currentPrice = "220$";
    var priceDisplay = document.getElementById('price-display');
    priceDisplay.textContent = currentPrice;
    var currentPriceSpan = document.getElementById('current-price');
    currentPriceSpan.style.display = 'none';
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
    let stockName = getStockName(); // Ersetze dies mit dem Namen des Eingabefelds für das Stock Symbol
    let url = `https://api.polygon.io/v2/aggs/ticker/${stockName}/prev?adjusted=true&apiKey=Vf080TfqbqvnJHcpt2aP9Ec1XL21Xb0D`;

    $.ajax({
        url: url,
        method: 'GET',
        dataType: 'json',
        success: function(data) {
            console.log(data); // Debugging: Überprüfe die Datenstruktur

            if (data.status === 'OK' && data.results && data.results.length > 0) {
                const closeValue = parseFloat(data.results[0].c); // Der Schlusskurs
                const roundedCloseValue = closeValue.toFixed(2);
                $('#price-display').text(`${roundedCloseValue}$`);
            } else if (!data.results) {
                $('#price-display').text('Aktie nicht gefunden oder keine Daten verfügbar.');
            } else if (data.results.length === 0) {
                $('#price-display').text('Keine Schlusskursdaten verfügbar.');
            } else {
                $('#price-display').text('Unbekannter Fehler beim Abrufen der Daten.');
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            if (jqXHR.status === 429) {
                $('#price-display').text('Zu viele Anfragen. Bitte versuche es später erneut.');
            } else {
                console.error('Fehler:', textStatus, errorThrown);
                $('#price-display').text('Fehler beim Abrufen der Daten.');
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