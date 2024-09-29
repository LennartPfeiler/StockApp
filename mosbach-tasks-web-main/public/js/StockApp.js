const APIKEY = 'Vf080TfqbqvnJHcpt2aP9Ec1XL21Xb0D'; // Externer API-Schlüssel

///* Login *//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function login(profileSchema){
    setCookie("email", profileSchema.email.value);
    let password = profileSchema.password.value;
    let answer = getProfileByEmail();
    if(answer.success == true){
        if(password != getCookie("password"))
            alert("Das Passwort ist falsch versuche es erneut!");
        else{
            document.location = "home.html";
        }
    }
    else{
        alert(answer.message);
    } 
}

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
function getProfileByEmail(){
    const settingsGetProfile = {
        "async": false,
        "url": "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/user?email="+ getCookie("email"),
        "method": "GET",
        "headers": {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    }
    console.log(settingsGetProfile.url);
    $.ajax(settingsGetProfile).done(function (user) {
        let success = true;
        let message = "";
        if (user) {
            setCookie("userID", user.userID);
            setCookie("firstName", user.firstName);
            setCookie("lastName", user.lastName);
            setCookie("email", user.email);
            setCookie("password", user.password);
            return {success: true, message: "Datenabruf erfolgreich!"}
        } else {
            return {success: false, message: "Bitte registrieren sie sich zuerst!"}
        }
    }).fail(function() {
        return {success: false, message: "Fehler beim Abrufen der Benutzerdaten. Bitte versuchen Sie es später erneut."}
    });
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
            } else if (jqXHR.status === 404) {
                $('#price-display').text('Aktie nicht gefunden.');
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
