<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/promise-polyfill@8/dist/polyfill.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@10"></script>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Chating</title>
    <link rel="stylesheet" href="/css/mafiaChat.css">
    <script src="/js/lunchChat.js"></script>
    <script src="/js/lunchPlay.js"></script>
</head>
<body onunload="unload()">
<div id="container" class="container">
    <h1 id="chatRoomHeader">${roomName}</h1>
    <p id="announce"></p>
    <input type="hidden" id="sessionId" value="">
    <input type="hidden" id="userId" value="${userId}">
    <input type="hidden" id="roomName" value="${roomName}">
    <input type="hidden" id="roomNumber" value="${roomNumber}">
    <input type="hidden" id="roomId" value="${roomId}">
    <input type="hidden" id="myJob" value="mafia">

    <div class="chatAndList">
        <div id="chating" class="chating">
        </div>
        <div id="memberList" class="memberList">
        </div>
    </div>

    <div id="yourName">
        <table class="inputTable">
            <tr>
                <th>사용자명</th>
                <td><input type="text" name="userName" id="userName"></td>
                <td><button onclick="resendChatName()" id="startBtn">이름 등록</button></td>
            </tr>
        </table>
    </div>
    <div id="yourMsg">
        <table class="inputTable">
            <tr id="uiBtn">
            </tr>
        </table>
        <table class="inputTable">
            <tr>
                <td>메뉴</td>
                <td><button onclick="backToRoomList()" id="backBtn">나가기</button></td>
            </tr>
        </table>
    </div>
    <div class="voteList" id="voteList" onclick="closeVoteList()">
        <div class="voteList-content">
            <span class="voteList-close">&times;</span>
            <p>투표 할 음식점을 클릭해주세요</p>
            <p id="memberNameBtn">Some text in the Modal..</p>
        </div>
    </div>
</div>
</body>
</html>