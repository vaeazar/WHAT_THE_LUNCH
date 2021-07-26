var socketVar;
var jbRandom = Math.random();
let voteCompFlag = false;
let staticScrollFlag = true;
let isFocused = true;
const testFlag = true;
var testval1;
var testval2;
var testval3;
var testval4;
var testval5;

document.addEventListener("DOMContentLoaded", function () {
  //checkRefresh();
  wsOpen();

  if ( window.history.replaceState ) {
    window.history.replaceState( null, null, 'room' );
  }

  document.onkeydown = function (e) {
    /* F5, Ctrl+r, Ctrl+F5 */
    if (e.keyCode == 116 || e.ctrlKey == true && (e.keyCode == 82)) {
      let freshOk = confirm('새로고침 시 방 목록으로 돌아갑니다.');
      if (freshOk) {
        location.href = '/room';
      }
      return false;
    }
  }

  window.onbeforeunload = function (e) {
    let freshOk = confirm('새로고침 시 방 목록으로 돌아갑니다.');
    if (freshOk) {
      location.href = '/room';
    }
    return false;
  };

  $('#chating').scroll(function(){
    var scrollTop = $(this).scrollTop();
    var innerHeight = $(this).innerHeight();
    var scrollHeight = $(this).prop('scrollHeight');

    if (scrollTop + innerHeight == scrollHeight) {
      thisMemberRead();
    }
  });
  // document.querySelector('#mafiaChatTh').remove();
  // document.querySelector('#mafiaChatTd').remove();

  // if (Math.floor(jbRandom * 2) == 1) {
  //   $('#myJob').val('mafia');
  // } else {
  //   if (testFlag) {
  //     $('#myJob').val('mafia');
  //   } else {
  //     $('#myJob').val('citizen');
  //     document.querySelector('#mafiaChatTh').remove();
  //     document.querySelector('#mafiaChatTd').remove();
  //   }
  // }
});

function wsOpen() {
  //socketVar = new WebSocket("ws://" + location.host + "/chating");
  socketVar = new WebSocket(
      "ws://" + location.host + "/chating/" + $("#roomId").val() + "_" + $(
      "#userId").val());
  wsEvt();
}


window.addEventListener("focus", function(event)
{
  setFocuse();
});

window.addEventListener("blur", function(event)
{
  releaseFocuse();
});

function wsEvt() {
  socketVar.onopen = function (data) {
    //소켓이 열리면 초기화 세팅하기
    chatName();
  }

  socketVar.onmessage = function (data) {
    var msg = data.data;
    if (msg != null && msg.trim() != '') {
      var jsonTemp = JSON.parse(msg);
      if (jsonTemp.type == "getId") {
        var sessionId = jsonTemp.sessionId != null ? jsonTemp.sessionId : "";
        if (sessionId != '') {
          $("#sessionId").val(sessionId);
        }
        if (jsonTemp.isAdmin) {
          $('.adminTable').show();
          let tempHtml = '<tr>';
          tempHtml += '<td style="color: black;"><input type="checkbox" id="visitStore">최근 제외</td>';
          tempHtml += '<td><button onclick="startGame()" id="startBtn" class="startBtn">추천 받기</button></td>';
          tempHtml += '<td><button onclick="voteStart()">투표 개시</button></td>';
          tempHtml += '<td><button onclick="tempKillBtn()">투표 완료</button></td>';
          // tempHtml += '<td><button onclick="mafiaVote()">익명 투표 개시</button></td>';
          // tempHtml += '<td><button onclick="tempMafiaKillBtn()">익명 투표 완료</button></td>';
          tempHtml += '</tr>';
          tempHtml += '<tr>';
          tempHtml += '<td style="color: black;">국가</td>';
          tempHtml += '<td style="color: black;"><input type="checkbox" class="nationCheck" value="korea">한식</td>';
          tempHtml += '<td style="color: black;"><input type="checkbox" class="nationCheck" value="china">중식</td>';
          tempHtml += '<td style="color: black;"><input type="checkbox" class="nationCheck" value="japan">일식</td>';
          tempHtml += '<td style="color: black;"><input type="checkbox" class="nationCheck" value="etc">기타</td>';
          tempHtml += '</tr>';
          tempHtml += '<tr>';
          tempHtml += '<td style="color: black;">종류</td>';
          tempHtml += '<td style="color: black;"><input type="checkbox" class="kindCheck" value="rice">밥</td>';
          tempHtml += '<td style="color: black;"><input type="checkbox" class="kindCheck" value="noodle">면</td>';
          tempHtml += '<td style="color: black;"><input type="checkbox" class="kindCheck" value="soup">국/찌개</td>';
          tempHtml += '<td style="color: black;"><input type="checkbox" class="kindCheck" value="snack">분식</td>';
          tempHtml += '<td style="color: black;"><input type="checkbox" class="kindCheck" value="etc">기타</td>';
          tempHtml += '</tr>';
          $("#uiBtnAdmin").append(tempHtml);
        }
      } else if (jsonTemp.type == "memberList") {
        let chatColor = $('input[name=othersChatColor]:checked').val();
        $("#chating").append(
            "<p class='newMemberJoin'>" + decodeURI(jsonTemp.newMemberName,'UTF-8') + " 님께서 입장하셨습니다.</p>");
        scrollDown(staticScrollFlag);
        let memberList = jsonTemp.memberList;
        let memberListTag = '';
        memberList.forEach(function(item,index,arr) {
          memberListTag += "<p class='member'>" + decodeURI(item.memberName,'UTF-8') + "</p>";
        });
        $("#memberList").html(memberListTag);
        $("#memberList").scrollTop($("#chating")[0].scrollHeight);
      } else if (jsonTemp.type == "memberOut") {
        let chatColor = $('input[name=othersChatColor]:checked').val();
        $("#chating").append(
            "<p class='newMemberJoin'>" + decodeURI(jsonTemp.outMemberName,'UTF-8') + " 님께서 퇴장하셨습니다.</p>");
        scrollDown(staticScrollFlag);
        let memberList = jsonTemp.memberList;
        let memberListTag = '';
        memberList.forEach(function(item,index,arr) {
          memberListTag += "<p class='member'>" + decodeURI(item.memberName,'UTF-8') + "</p>";
        });
        $("#memberList").html(memberListTag);
        $("#memberList").scrollTop($("#chating")[0].scrollHeight);

        if (!voteCompFlag) {
          let btnContain = document.querySelector('#memberNameBtn');
          let target = btnContain.querySelector('.player_' + jsonTemp.outMemberName);
          if (target != undefined) target.remove();
        }
      } else if (jsonTemp.type == "mafiaKillComplete") {
        $('#modalBtn').remove();
        $("#chating").append(
            "<p class='newMemberJoin' style='color:#ffffff;'>====================================</p>");
        $("#chating").append(
            "<p class='newMemberJoin' style='color:#ffffff;'>투표 결과 : " + decodeURI(jsonTemp.storeName,'UTF-8') + "("+jsonTemp.storeCount+")</p>");
        if (jsonTemp.storeMenuUrl != '') {
          $("#chating").append(
              "<p class='newMemberJoin' style='color:#ffffff;'>메뉴(클릭 시 큰 이미지)</br><img class='storeMenu' onclick='javascript:window.open(\""+jsonTemp.storeMenuUrl+"\")' src='"+jsonTemp.storeMenuUrl+"'></p>");
        }
        $("#chating").append(
            "<p class='newMemberJoin' style='color:#ffffff;'>====================================</p>");
        scrollDown(staticScrollFlag);
        $('#modalBtn').remove();
        voteCompFlag = false;
      } else if (jsonTemp.type == "adminLeft") {
        $('.adminTable').show();
        let tempHtml = '<tr>';
        tempHtml += '<td style="color: black;"><input type="checkbox" id="visitStore">최근 제외</td>';
        tempHtml += '<td><button onclick="startGame()" id="startBtn" class="startBtn">추천 받기</button></td>';
        tempHtml += '<td><button onclick="voteStart()">투표 개시</button></td>';
        tempHtml += '<td><button onclick="tempKillBtn()">투표 완료</button></td>';
        // tempHtml += '<td><button onclick="mafiaVote()">익명 투표 개시</button></td>';
        // tempHtml += '<td><button onclick="tempMafiaKillBtn()">익명 투표 완료</button></td>';
        tempHtml += '</tr>';
        tempHtml += '<tr>';
        tempHtml += '<td style="color: black;">국가</td>';
        tempHtml += '<td style="color: black;"><input type="checkbox" class="nationCheck" value="korea">한식</td>';
        tempHtml += '<td style="color: black;"><input type="checkbox" class="nationCheck" value="china">중식</td>';
        tempHtml += '<td style="color: black;"><input type="checkbox" class="nationCheck" value="japan">일식</td>';
        tempHtml += '<td style="color: black;"><input type="checkbox" class="nationCheck" value="etc">기타</td>';
        // tempHtml += '<td><button onclick="mafiaVote()">익명 투표 개시</button></td>';
        // tempHtml += '<td><button onclick="tempMafiaKillBtn()">익명 투표 완료</button></td>';
        tempHtml += '</tr>';
        tempHtml += '<tr>';
        tempHtml += '<td style="color: black;">종류</td>';
        tempHtml += '<td style="color: black;"><input type="checkbox" class="kindCheck" value="rice">밥</td>';
        tempHtml += '<td style="color: black;"><input type="checkbox" class="kindCheck" value="noodle">면</td>';
        tempHtml += '<td style="color: black;"><input type="checkbox" class="kindCheck" value="soup">국/찌개</td>';
        tempHtml += '<td style="color: black;"><input type="checkbox" class="kindCheck" value="snack">분식</td>';
        tempHtml += '<td style="color: black;"><input type="checkbox" class="kindCheck" value="etc">기타</td>';
        // tempHtml += '<td><button onclick="mafiaVote()">익명 투표 개시</button></td>';
        // tempHtml += '<td><button onclick="tempMafiaKillBtn()">익명 투표 완료</button></td>';
        tempHtml += '</tr>';
        if (jsonTemp.isAdmin) {
          $("#uiBtnAdmin").append(tempHtml);
        }
      } else if (jsonTemp.type == "roomIsStart") {
        //timer(5,0,'낮 시간');
        let param = {
          roomId : $('#roomId').val(),
          playerId : $('#userId').val()
        };
        // commonAjax('/getMafiaList', param, 'post', function () {
        // });
      } else if (jsonTemp.type == 'gameStart') {
        $('#recommTd').remove();
        let recommMenu = JSON.parse(jsonTemp.storeList);
        let recommHtml = '';
        $("#recommListContainer").html('');
        recommMenu.forEach(function(val,idx,arr) {
          if (val.storeNaver != '') {
            recommHtml +=
                "<p class='newMemberJoin' style='color:#ffffff;'>" + val.storeComment + " "
                + "<span class='recommElement recommNaver' onclick='openInNewTab(\""+val.storeNaver+"\")'>"
                + val.storeName + "</span></p>";
          } else {
            recommHtml +=
                "<p class='newMemberJoin' style='color:#ffffff;'>" + val.storeComment + " "
                + "<span class='recommElement'>"
                + val.storeName + "</span></p>";
          }
        });
        $("#recommListContainer").html(recommHtml)
        voteOpen(recommMenu)
        scrollDown(staticScrollFlag);
        openRecommList();
        let tempHtml = '';
        tempHtml += '<td id="recommTd"><button id="recommBtn" onclick="openRecommList()">추천 목록</button></td>';
        $("#uiBtn2").append(tempHtml);
      } else if (jsonTemp.type == "voteStarted") {
        $('#modalBtn').remove();
        let tempHtml = '';
        tempHtml += '<td><button id="modalBtn" onclick="tempVoteClick()">투표</button></td>';
        $("#uiBtn2").append(tempHtml);
        $("#chating").append(
            "<p class='newMemberJoin' style='color:red;'>투표가 시작되었습니다.</p>");
        scrollDown(staticScrollFlag);
      } else if (jsonTemp.type == "voteComplete") {
        $("#chating").append(
            "<p class='newMemberJoin' style='color:#abff93;'>" + decodeURI(jsonTemp.memberId,'UTF-8') + "님이 투표하였습니다."
            + "</br>"+jsonTemp.voteCount+" 명 투표 완료</p>");
        scrollDown(staticScrollFlag);
      } else if (jsonTemp.type == "fail") {
        if (jsonTemp.failReason == 'nameExist') {
          $("#yourMsg").hide();
          $("#yourName").show();
          Swal.fire({
            icon: 'error',
            allowOutsideClick: false,
            text: jsonTemp.failMessage
          })
          $("#userId").focus();
        } else if (jsonTemp.failReason == 'fullBang') {
          $("#yourMsg").hide();
          Swal.fire({
            icon: 'error',
            text: jsonTemp.failMessage,
            allowOutsideClick: false,
            footer: '<a href="/room">방 목록으로 이동</a>'
          }, function (isConfirm) {
            if (isConfirm) {
              location.href = '/room'; //브라우저가 지원하지 않는 경우 페이지 이동처리
            } else {
              location.href = '/room'; //브라우저가 지원하지 않는 경우 페이지 이동처리
            }
          });
          //location.href("/room");
        } else if (jsonTemp.failReason == 'joinFailed') {
          $("#yourMsg").hide();
          Swal.fire({
            icon: 'error',
            text: jsonTemp.failMessage,
            allowOutsideClick: false,
            footer: '<a href="/room">방 목록으로 이동</a>'
          }, function (isConfirm) {
            if (isConfirm) {
              location.href = '/room'; //브라우저가 지원하지 않는 경우 페이지 이동처리
            } else {
              location.href = '/room'; //브라우저가 지원하지 않는 경우 페이지 이동처리
            }
          });
        } else if (jsonTemp.failReason == 'deletedRoom') {
          $("#yourMsg").hide();
          Swal.fire({
            icon: 'error',
            text: jsonTemp.failMessage,
            allowOutsideClick: false,
            footer: '<a href="/room">방 목록으로 이동</a>'
          }, function (isConfirm) {
            if (isConfirm) {
              location.href = '/room'; //브라우저가 지원하지 않는 경우 페이지 이동처리
            } else {
              location.href = '/room'; //브라우저가 지원하지 않는 경우 페이지 이동처리
            }
          });
        }
      } else if (jsonTemp.type == "message") {
        if (jsonTemp.sessionId == $("#sessionId").val()) {
          let chatColor = $('input[name=myChatColor]:checked').val();
          if (isFocused) {
            $("#chating").append("<p class='me' style='color:"+chatColor+"\;'>" + jsonTemp.msg
                + "</br><span class='newMessage readCount "+jsonTemp.readCheck+"'>"+jsonTemp.readCount+"</span> <span class='chatTime'>"+jsonTemp.nowTime+"</span></p>");
            if (jsonTemp.msg.indexOf('#구분') != -1) {
              $("#chating").append(
                "<p class='newMemberJoin' style='color:#ffffff;'><img class='storeMenu' src='https://vaeazar123.github.io/selectBtn.JPG'></p>");
            }
          } else {
            var chatCount  = parseInt(document.title.substring(1,document.title.indexOf(')'))) +1;
            document.title = '('+chatCount+')Chating';
            $("#chating").append("<p class='me' style='color:"+chatColor+"\;'>" + jsonTemp.msg
                + "</br><span class='newMessage readCount "+jsonTemp.readCheck+"'>"+(jsonTemp.readCount+1)+"</span> <span class='chatTime'>"+jsonTemp.nowTime+"</span></p>");
          }
          scrollDownChatMessage(staticScrollFlag);
        } else {
          let chatColor = $('input[name=othersChatColor]:checked').val();
          if (isFocused) {
            $("#chating").append(
                "<p class='others' style='color:"+chatColor+"\;'>" + jsonTemp.userId + " :" + jsonTemp.msg
                + "</br><span class='chatTime'>"+jsonTemp.nowTime+"</span> <span class='newMessage readCount "+jsonTemp.readCheck+"'>"+jsonTemp.readCount+"</span></p>");
          } else {
            var chatCount  = parseInt(document.title.substring(1,document.title.indexOf(')'))) +1;
            document.title = '('+chatCount+')Chating';
            $("#chating").append(
                "<p class='others' style='color:"+chatColor+"\;'>" + jsonTemp.userId + " :" + jsonTemp.msg
                + "</br><span class='chatTime'>"+jsonTemp.nowTime+"</span> <span class='newMessage readCount "+jsonTemp.readCheck+"'>"+jsonTemp.readCount+"</span></p>");
          }
          scrollDownChatMessage(staticScrollFlag);
        }
      } else if (jsonTemp.type == "mafia") {
        if ($('#myJob').val() != 'mafia') {
          return false;
        }
        if (jsonTemp.sessionId == $("#sessionId").val()) {
          $("#chating").append("<p class='me-mafia'>" + jsonTemp.msg + "</p>");
          scrollDown(staticScrollFlag);
        } else {
          $("#chating").append(
              "<p class='others-mafia'>(마피아)" + jsonTemp.userId + " :"
              + jsonTemp.msg + "</p>");
          scrollDown(staticScrollFlag);
        }
      } else if (jsonTemp.type == "showyourjob"){
        console.log(jsonTemp.msg);
        var jobs = jsonTemp.msg;
        var userId = $("#userId").val();
        var result = determineJob(jobs,userId);
        $("#chating").append(
            "<p class='show-your-job' style='color:"+result.chatColor+";'>당신의 직업은 "+result.yourJob+" 입니다.</p>"
        );
      } else if (jsonTemp.type == "timeCountDown"){
        $("#announce").text(jsonTemp.msg);
      } else if (jsonTemp.type == "moderator") { //moderator : 사회자
        $("#chating").append(
            "<p class='moderator' style='color:white;'>"+jsonTemp.msg+"</p>"
        );
      } else if (jsonTemp.type == "thisMemberRead"){
        let memberNames = document.getElementsByClassName(jsonTemp.readMember);
        let idxLimit = memberNames.length;
        for (idx = 0; idx < idxLimit; idx++) {
          memberNames[idx].textContent = memberNames[idx].textContent - 1;
        }
        $(memberNames).removeClass(jsonTemp.readMember);
      } else {
        console.warn("unknown type!")
      }
    }
  }

  document.addEventListener("keypress", function (e) {
    if (e.keyCode == 13) { //enter press
      send();
    }
  });
}

function chatName() {
  var userId = $("#userId").val();
  if (userId == null || userId.trim() == "") {
    alert("사용자 이름을 입력해주세요.");
    $("#userName").focus();
    //location.href = '/room';
  } else {
    $("#yourName").hide();
    $("#yourMsg").show();
  }
}

function resendChatName() {
  var userName = $("#userName").val();
  if(userName == null || userName.trim() == ""){
    alert("사용자 이름을 입력해주세요.");
    $("#userName").focus();
  }else{
    document.querySelector('#userId').value = userName;
    wsOpen();
    $("#yourName").hide();
    $("#yourMsg").show();
  }
}

function send() {
  if ($('#chatting').val() == "") {
    return false;
  }
  var option;
  if ($('#mafiaChat').prop("checked")) {
    option = {
      type: "mafia",
      roomNumber: $("#roomNumber").val(),
      roomId: $("#roomId").val(),
      sessionId: $("#sessionId").val(),
      userId: $("#userId").val(),
      msg: $("#chatting").val()
    }
  } else {
    option = {
      type: "message",
      roomNumber: $("#roomNumber").val(),
      roomId: $("#roomId").val(),
      sessionId: $("#sessionId").val(),
      userId: $("#userId").val(),
      msg: $("#chatting").val()
    }
  }
  socketVar.send(JSON.stringify(option));
  $('#chatting').val("");
}

function backToRoomList() {
  $(location).attr('href', "/");
}

function startGame() {
  var option = '';
  var nation = '';
  var kind = '';
  $("input[class=nationCheck]:checked").each(function() {
    nation += $(this).val();
    nation += '|';
  });
  nation = nation.substring(0,nation.length-1);
  $("input[class=kindCheck]:checked").each(function() {
    kind += $(this).val();
    kind += '^';
  });
  kind = kind.substring(0,kind.length-1);
  if ($('#visitStore').prop("checked")) {
    option = {
      roomId: $('#roomId').val(),
      roomName: $('#roomName').val(),
      nation: nation,
      kind: kind,
      weekExcept: 'Y'
    };
  } else {
    option = {
      roomId: $('#roomId').val(),
      roomName: $('#roomName').val(),
      nation: nation,
      kind: kind,
      weekExcept: 'N'
    };
  }

  commonAjax('/setRoomStart', option, 'post', function () {
    //$("#startBtn").remove();
  });

  var option;
  option = {
    type: "roomIsStart",
    roomId: $('#roomId').val()
  }
  socketVar.send(JSON.stringify(option));
}


function startGameMafia() {
  var roomId = {roomId: $('#roomId').val()};
  var memberCnt = $(".member").length;
  var mafia = '';
  //테스트 위해서 8명 이하여도 게임 시작되게 임시로 수정
  /*if (memberCnt<8) {
    $("#chating").append("<p class='game-not-start' style='color:white;'>게임 시작에는 8명 이상이 필요합니다!</p>");
  } else {
    commonAjax('/setRoomStart', roomId, 'post', function () {
      $("#startBtn").remove();
    });
    showYourJob();
  }*/

  commonAjax('/setRoomStart', roomId, 'post', function () {
    //$("#startBtn").remove();
  });
  //showYourJob();

  var option;
  option = {
    type: "roomIsStart",
    roomId: $('#roomId').val()
  }
  socketVar.send(JSON.stringify(option));
}

// 타이머 function

// Set the date we're counting down to
function timer(min,sec,type) {

  var countDownDate = new Date();
  countDownDate.setMinutes(countDownDate.getMinutes() + min);
  countDownDate.setSeconds(countDownDate.getSeconds() + sec);

// Update the count down every 1 second
  var x = setInterval(function () {

    // Get today's date and time
    var now = new Date();

    // Find the distance between now and the count down date
    var distance = countDownDate - now;
    // Time calculations for days, hours, minutes and seconds
    // var days = Math.floor(distance / (1000 * 60 * 60 * 24));
    // var hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    var minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
    var seconds = Math.floor((distance % (1000 * 60)) / 1000);

    // Display the result in the element with id="demo"
    var announce = type + "시간 " + minutes + "분 "
        + seconds + "초 남았습니다.";
    var option = {
      type: "timeCountDown",
      roomNumber: $("#roomNumber").val(),
      roomId: $("#roomId").val(),
      sessionId: $("#sessionId").val(),
      userId: $("#userId").val(),
      msg: announce
    };
    // If the count down is finished, write some text
    if (distance < 0) {
      clearInterval(x);
      document.getElementById("announce").innerHTML = "EXPIRED";
      voteOpen();
    } else {
      socketVar.send(JSON.stringify(option));
    }
  }, 1000);
}

function commonAjax(url, parameter, type, calbak, contentType) {
  $.ajax({
    url: url,
    data: parameter,
    type: type,
    contentType: contentType != null ? contentType
        : 'application/x-www-form-urlencoded; charset=UTF-8',
    success: function (res) {
      calbak(res);
    },
    error: function (err) {
      console.log('error');
      calbak(err);
    }
  });
}

function checkRefresh() {
  if (window.performance) {
    if (performance.navigation.type == 1) {
      alert("This page is reloaded");
    } else {
      alert("This page is not reloaded");
    }
  }
}

function unload() {
  alert("unload 됩니다.")

  if (self.screenTop > 9000) {
    // 브라우저 닫힘
    alert("브라우저가 닫힘.")
  } else {
    if (document.readyState == "complete") {
      // 새로고침
      alert("새로고침")
    } else if (document.readyState == "loading") {
      // 다른사이트로 이동
      alert("다른사이트로 이동");
    }
  }
}

function showYourJob() {
  var request = new XMLHttpRequest();
  request.onload = function () {
    if (request.status == 200) {
      var str = request.responseText;
      var jobs = JSON.parse(str);
      var option;
      option = {
        type: "showyourjob",
        roomNumber: $("#roomNumber").val(),
        roomId: $("#roomId").val(),
        sessionId: $("#sessionId").val(),
        userId: $("#userId").val(),
        msg: jobs
      };
      socketVar.send(JSON.stringify(option));
    }
  }
  request.open('GET',"/startGame/"+$("#roomId").val(),true);
  request.send();
}

var determineJob = function(jobs, userId) {
  var yourJob = "";
  var chatColor = "white";
  for (var i in jobs) {
    console.log(jobs[i].name+" "+userId);
    if (jobs[i].name == userId) {
      yourJob = jobs[i].job;
      if (yourJob == "mafia") {
        chatColor = "red";
      }
      break;
    }
  }
  return {
    yourJob : yourJob,
    chatColor : chatColor
  };
};

function gameProcess() {
  morning();
  election();
  execution();
  night();
}

function morning() {
  timer(5,0,"낮");
  option = {
    type: "moderator",
    roomNumber: $("#roomNumber").val(),
    roomId: $("#roomId").val(),
    sessionId: $("#sessionId").val(),
    userId: $("#userId").val(),
    msg: "낮이 다되었습니다. 투표를 해주세요"
  };
  socketVar.send(JSON.stringify(option));
}

function election() {

}
function voteOpen(recommMenu) {
  let btnHtml = '';
  recommMenu.forEach(function(val,idx,arr) {
    btnHtml += "<p><button onclick='clickStore(\""+val.storeName+"\")' class='voteBtn player_"+val.storeName+"'>"+val.storeName+"</button></p>";
  });
  $('#memberNameBtn').html(btnHtml);
}

function mafiaVoteOpen() {
  let roomId = {roomId: $('#roomId').val()};
  commonAjax('/getCivilNames', roomId, 'post', function (result) {
    let tempJson = JSON.parse(result);
    let tempList = tempJson.memberList;
    let btnHtml = '';
    tempList.forEach(function (e,i,a) {
      btnHtml += "<button onclick='clickStore(\""+e+"\")' class='voteBtn player_"+e+"'>"+e+"</button>";
    });
    $('#memberNameBtn').html(btnHtml);
  });
}

function clickStore(storeName) {
  let param = {
    roomId : $('#roomId').val(),
    memberId : $('#userId').val(),
    storeName : storeName
  };
  let btnHtml = '';
  commonAjax('/clickStore', param, 'post', function () {
    btnHtml += "<p>투표 : "+storeName+"</p>";
    $('#memberNameBtn').html(btnHtml);
    $('#modalBtn').remove();
    voteCompFlag = true;
  });
}

function tempKillBtn() {
  let param = {
    roomId : $('#roomId').val()
  };
  commonAjax('/mafiaKill', param, 'post', function () {
    voteCompFlag = false;
  });
}

function voteStart() {
  var option;
  option = {
    type: "voteStart",
    roomId: $('#roomId').val()
  }
  socketVar.send(JSON.stringify(option));
  commonAjax('/voteStart', option, 'post', function () {});
}

function mafiaVote() {
  var option;
  option = {
    type: "mafiaVote",
    roomId: $('#roomId').val()
  }
  socketVar.send(JSON.stringify(option));
}

function tempMafiaKillBtn() {
  let param = {
    roomId : $('#roomId').val()
  };
  commonAjax('/mafiaKill', param, 'post', function () {
  });
}

function cleanChatSpace() {
  $('#chating').html('');
}

function scrollDown(scrollFlag) {
  if (scrollFlag) {
    $("#chating").scrollTop($("#chating")[0].scrollHeight);
  }
}

function scrollDownChatMessage(scrollFlag) {
  var checkScrollExist = $('#chating').prop('scrollHeight');
  if (checkScrollExist == 570 && isFocused) {
    thisMemberRead();
  }
  if (scrollFlag) {
    $("#chating").scrollTop($("#chating")[0].scrollHeight);
  }
}

function changeScroll() {
  if ($("input[class=scrollFlag]:checked").length == 0) {
    staticScrollFlag = false;
  } else {
    staticScrollFlag = true;
  }
}

function setFocuse() {
  isFocused = true;

  var checkScrollExist = $('#chating').prop('scrollHeight');
  if (checkScrollExist == 570) {
    thisMemberRead();
  }
  if ($("input[class=scrollFlag]:checked").length == 0) {
    staticScrollFlag = false;
  } else {
    staticScrollFlag = true;
  }
}

function releaseFocuse() {
  isFocused = false;
  staticScrollFlag = false;
}

function thisMemberRead() {
  var option;
  option = {
    type: "thisMemberRead",
    roomNumber: $("#roomNumber").val(),
    roomId: $("#roomId").val(),
    sessionId: $("#sessionId").val(),
    userId: $("#userId").val()
  }
  socketVar.send(JSON.stringify(option));
  document.title = '(0)Chating';
}