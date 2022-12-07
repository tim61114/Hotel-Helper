async function getMap() {
    let config = {
        minZoom: 7,
        maxZoom: 18,
    };
// magnification with which the map will start
    const zoom = 18;
    let response = await fetch('/coor', {method :'get'});
    let coor = await response.json();
    let lat = coor['lat'];
    let lng = coor['lng'];
    let map = L.map("map", config).setView([lat, lng], zoom);
    L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution:
            '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(map);

    let marker = L.marker([lat, lng]).addTo(map);

}
getMap()