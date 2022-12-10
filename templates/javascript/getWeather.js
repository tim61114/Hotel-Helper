async function getWeather() {

// magnification with which the map will start
    let response = await fetch('/coor', {method :'get'});
    let coor = await response.json();
    let lat = coor['lat'];
    let lng = coor['lng'];
    let weather = await fetch('https://api.open-meteo.com/v1/forecast' +
    "?latitude=" + lat + "&longitude=" + lng + "&current_weather=true" , {method :'get'});

    let weatherJson = await weather.json();
    document.getElementById("weather").innerHTML =
        "Current Temperature: " + weatherJson['current_weather']['temperature'] +
        ", Current Windspeed: " + weatherJson['current_weather']['windspeed'];

}
getWeather()
setInterval(getWeather, 10000)

