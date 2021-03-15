document.addEventListener("DOMContentLoaded", function () {
  var modal = document.getElementById('voteList');
  var recommModal = document.getElementById('recommList');
  //var btn = document.getElementById("modalBtn");
  var span = document.getElementsByClassName("voteList-close")[0];
  var recommSpan = document.getElementsByClassName("recommList-close")[0];

  // btn.onclick = function() {
  //   voteOpen();
  //   modal.style.display = "block";
  // }

  span.onclick = function() {
    modal.style.display = "none";
  }
  recommSpan.onclick = function() {
    recommModal.style.display = "none";
  }

  var childVoteList = document.querySelector('.voteList-content');
  var childRecommList = document.querySelector('.recommList-content');

  childVoteList.onclick = function(e) {
    e.stopPropagation();
  }
  childRecommList.onclick = function(e) {
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

function openInNewTab(url) {
  var win = window.open(url, '_blank');
  win.focus();
}
