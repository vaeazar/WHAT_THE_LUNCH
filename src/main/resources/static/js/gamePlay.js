document.addEventListener("DOMContentLoaded", function () {
  var modal = document.getElementById('voteList');
  //var btn = document.getElementById("modalBtn");
  var span = document.getElementsByClassName("voteList-close")[0];

  // btn.onclick = function() {
  //   voteOpen();
  //   modal.style.display = "block";
  // }

  span.onclick = function() {
    modal.style.display = "none";
  }

  var childVoteList = document.querySelector('.voteList-content');

  childVoteList.onclick = function(e) {
    e.stopPropagation();
  }
});

function tempVoteClick() {
  var modal = document.getElementById('voteList');
  modal.style.display = "block";
}

function mafiaVoteClick() {
  var modal = document.getElementById('voteList');
  modal.style.display = "block";
}

function closeVoteList() {
  var modal = document.getElementById('voteList');
  modal.style.display = "none";
}

function openRecommList() {
  var modal = document.getElementById('recommList');
  modal.style.display = "block";
}

function closeRecommList() {
  var modal = document.getElementById('recommList');
  modal.style.display = "none";
}
