async function checkLike() {
    let response = await fetch('/check_favorite', {method :'get'});
    let json = await response.json()

    if(json.isFavorite) {
        document.getElementById("like").innerHTML = "Remove from My Favorite Hotels";
    } else {
        document.getElementById("like").innerHTML = "Add to My Favorite Hotels";
    }
}

async function likeOrUnlike() {
    await fetch('/favorites', {method : 'post'});
    await checkLike()
}