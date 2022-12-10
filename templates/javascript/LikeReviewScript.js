async function getLikes(reviewId) {
    let response = await fetch('/review_num_likes?reviewId=' + reviewId, {method : 'get'});
    let json = await response.json();

    if (json.numLikes === 1) {
        document.getElementById("helpful" + reviewId).innerHTML = "1 Person found this review helpful";
    } else if (json.numLikes > 1) {
        document.getElementById("helpful" + reviewId).innerHTML = json.numLikes + " People found this review helpful";
    } else if (json.numLikes === 0) {
        document.getElementById("helpful"+ reviewId).innerHTML = "";
    }
}

async function checkLikeReview(reviewId) {
    let response = await fetch('like_review?reviewId=' + reviewId, {method : 'get'});
    let json = await response.json();

    if (json.userLikes) {
        document.getElementById(reviewId).innerHTML = "Unlike";
    } else {
        document.getElementById(reviewId).innerHTML = "Like";
    }
}

async function likeOrUnlikeReview(reviewId) {
    await fetch('/like_review?reviewId=' + reviewId, {method : 'post'});
    await checkLikeReview(reviewId)
    await getLikes(reviewId)
}