///* Login *//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

let counter = 1;
function login(profileSchema){
    setCookie("userID", profileSchema.userID.value);
    let password = profileSchema.password.value;
    if(counter == 1){
        counter += 1;
        getProfile();
    }
    if(password != getCookie("password"))
        alert("Das Passwort ist falsch versuche es erneut!");
    else{
        counter = 1;
        document.location = "index.html";
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
function getProfile(){
    const settingsGetProfile = {
        "async": false,
        "url": "https://sharearide-carpool.herokuapp.com/api/v1.0/user?userID="+ getCookie("userID"),
        "method": "GET",
        "headers": {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    }
    $.ajax(settingsGetProfile).done(function (user) {
        setCookie("userID", user.userID);
        setCookie("firstName", user.firstName);
        setCookie("lastName", user.lastName);
        setCookie("eMail", user.eMail);
        setCookie("phone", user.phone);
        setCookie("password", user.password);
    });
}