package com.example.mafia.handler;

import com.example.mafia.dao.LunchEtcDao;
import com.example.mafia.dao.MemberDao;
import com.example.mafia.dao.RoomDao;
import com.example.mafia.domain.MafiaMessage;
import com.example.mafia.domain.Member;
import com.example.mafia.domain.Room;
import com.example.mafia.domain.Store;
import com.example.mafia.domain.Target;
import com.example.mafia.repository.MessageMongoDBRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Slf4j
public class SocketHandler extends TextWebSocketHandler {

  @Autowired
  RoomDao roomDao;

  @Autowired
  MemberDao memberDao;

  @Autowired
  LunchEtcDao lunchEtcDao;

  @Autowired
  private MessageMongoDBRepository messageMongoDBRepository;

  HashMap<String, WebSocketSession> sessionMap = new HashMap<>(); //웹소켓 세션을 담아둘 맵
  private static List<HashMap<String, Object>> rls = new ArrayList<>(); //웹소켓 세션을 담아둘 리스트 ---roomListSessions

  private static List<HashMap<String, Object>> players = new ArrayList<>(); //게임중인 플레이어 정보

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) {
    //메시지 발송
    try {
      String msg = message.getPayload();
      JSONObject obj = jsonToObjectParser(msg);

      String jsonGetType = (String) obj.get("type");
      String jsonGetRoomId = (String) obj.get("roomId");
      String jsonGetSessionId = (String) obj.get("sessionId");
      SimpleDateFormat timeFormat = new SimpleDateFormat("a hh:mm", Locale.KOREA);
      Calendar cal = Calendar.getInstance();
      String nowTime = timeFormat.format(cal.getTime());
      obj.put("nowTime",nowTime);
      HashMap<String, Object> temp = new HashMap<>();
      Room roomInfo = roomDao.selectRoomInfo(jsonGetRoomId);
      boolean mongoFlag = false;
      if (jsonGetType.equals("roomIsStart")) {
        roomInfo.setRoomId(jsonGetRoomId);
        roomInfo.setRoomStatus("start");
        roomDao.changeRoomStatus(roomInfo);

        JSONObject startObj = new JSONObject();
        startObj.put("type", "roomIsStart");
        messageSend(jsonGetRoomId, startObj);
      } else if (jsonGetType.equals("thisMemberRead")) {
        JSONObject startObj = new JSONObject();
        startObj.put("type", "thisMemberRead");
        String readMember = obj.get("userId") + "-readCheck";
        startObj.put("readMember", readMember);
        messageSend(jsonGetRoomId, startObj);
      } else if (jsonGetType.equals("voteStart")) {
        roomDao.roomVoteStart(jsonGetRoomId);

        JSONObject startObj = new JSONObject();
        startObj.put("type", "voteStarted");
        messageSend(jsonGetRoomId, startObj);
      } else if (jsonGetType.equals("mafiaVote")) {
        roomDao.roomMafiaVoteStart(jsonGetRoomId);

        JSONObject startObj = new JSONObject();
        startObj.put("type", "mafiaVoteStarted");
        messageSend(jsonGetRoomId, startObj);
      } else if (rls.size() > 0) {
        obj.put("readCount",roomInfo.getRoomCount()-1);
        mongoFlag = true;
        temp = rls.get(roomInfo.getSessionIdx());
        Member member = Member.builder()
            .memberRoomId(jsonGetRoomId)
            .memberName(memberDao.selectMemberName(jsonGetSessionId))
            .build();
        List<String> memberList = memberDao.selectMemberNamesExceptSender(member);
        StringBuffer memberNames = new StringBuffer();
        for (String tempMemberName : memberList) {
          memberNames.append(tempMemberName + "-readCheck ");
        }

        //해당 방의 세션들만 찾아서 메시지를 발송해준다.
        for (String k : temp.keySet()) {
          Boolean doNotSend = k.equals("roomId")          //방 고유 값
              || k.equals("adminSession")   //방장 아이디
              || k.equals("memberList")     //맴버 정보
              || k.equals("memberCount")    //방 인원 수
              || k.contains("_status")      //플레이어 상태 값
              || k.equals("voteStatus")      //플레이어 상태 값
              || k.equals("jobList")        //직업 리스트
              || k.equals("jobSessList")      //직업 리스트 sessionId
              || k.equals("roomStatus");    //방 상태 값

          if (doNotSend) { //방 정보 통과
            continue;
          }

          WebSocketSession wss = (WebSocketSession) temp.get(k);
          obj.put("readCheck",memberNames.toString());
          if (wss != null) {
            synchronized(wss) {
              wss.sendMessage(new TextMessage(obj.toJSONString()));
            }
          }
        }
      }

      if (mongoFlag) {
        MafiaMessage sendData = new MafiaMessage();
        sendData.setUserId(jsonGetSessionId);
        sendData.setMessage(obj.get("msg").toString());
        sendData.setRoomId(jsonGetRoomId);
        sendData.setRemoteIp(session.getRemoteAddress().toString());
        sendData.setLocalIp(session.getLocalAddress().toString());
        messageMongoDBRepository.insert(sendData);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    //소켓 연결
    super.afterConnectionEstablished(session);
    //sessionMap.put(session.getId(), session);
    boolean flag = true;
    JSONObject obj = new JSONObject();
    JSONObject listObj = new JSONObject();
    String url = session.getUri().toString();
    System.out.println(url);
    String tempUrlString[] = url.split("/chating/")[1].split("_");
    String roomId = "";
    String userName = "";
    if (tempUrlString.length > 1) {
      roomId = url.split("/chating/")[1].split("_")[0];
      userName = URLDecoder.decode(url.split("/chating/")[1].split("_")[1],"UTF-8");
    } else {
      roomId = "";
      userName = "";
    }
    int idx = rls.size() - 1; //방의 사이즈를 조사한다.
    Room roomInfo = roomDao.selectRoomInfo(roomId);
    if (!ObjectUtils.isEmpty(roomInfo) && roomInfo.getRoomCount() != 0) {
      idx = roomInfo.getSessionIdx();
    } else if (!ObjectUtils.isEmpty(roomInfo) && roomInfo.getRoomCount() == 0){
      roomInfo.setSessionIdx(idx);
      roomDao.changeRoomSessionIndex(roomInfo);
    } else {
      flag = false;
    }
    Member parameterMember = Member.builder()
        .memberName(userName)
        .memberRoomId(roomId)
        .memberAdminYN("N")
        .sessionIdx(idx)
        .build();
    String overlapName = memberDao.selectOverlapName(parameterMember);

    if (flag) { //존재하는 방이라면 세션만 추가한다.
      JSONObject failObj = new JSONObject();
      HashMap<String, Object> map = rls.get(idx);
      String roomStatus = StringUtils.isEmpty(roomInfo.getRoomStatus()) == true ? "start" : roomInfo.getRoomStatus();
      int memberCount = roomInfo.getRoomCount();
      if (memberCount > 14) {
        failObj.put("type", "fail");
        failObj.put("failReason", "fullBang");
        failObj.put("failMessage", "최대 인원이라 참가가 불가능합니다.");
        session.sendMessage(new TextMessage(failObj.toJSONString()));
        return;
      } else if (roomStatus.equals("start")) {
        failObj.put("type", "fail");
        failObj.put("failReason", "joinFailed");
        failObj.put("failMessage", "이미 시작 된 방입니다.");
        session.sendMessage(new TextMessage(failObj.toJSONString()));
        return;
      } else if (!StringUtils.isEmpty(overlapName)) {
        failObj.put("type", "fail");
        failObj.put("failReason", "nameExist");
        failObj.put("failMessage", "중복 된 이름이 있습니다.");
        session.sendMessage(new TextMessage(failObj.toJSONString()));
        return;
      } else if (memberCount == 0) {
        parameterMember.setMemberAdminYN("Y");
        obj.put("isAdmin", true);
      }
      listObj.put("type", "memberList");
      listObj.put("newMemberName", userName);
      map.put(session.getId(), session);
      parameterMember.setMemberStatus("alive");
      parameterMember.setMemberId(session.getId());
      roomDao.increaseRoomCount(roomId);
      memberDao.insert(parameterMember);
      List<String> memberNames = memberDao.selectMemberNames(roomId);
      listObj.put("memberList", memberNames);
    } else {
      JSONObject failObj = new JSONObject();
      failObj.put("type", "fail");
      failObj.put("failReason", "deletedRoom");
      failObj.put("failMessage", "존재하지 않는 방입니다.");
      session.sendMessage(new TextMessage(failObj.toJSONString()));
      return;
    }

    //세션등록이 끝나면 발급받은 세션ID값의 메시지를 발송한다.
    obj.put("type", "getId");
    obj.put("sessionId", session.getId());
    session.sendMessage(new TextMessage(obj.toJSONString()));
    messageSend(roomId,listObj);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    //소켓 종료
    //sessionMap.remove(session.getId());
    JSONObject obj = new JSONObject();
    JSONObject adminObj = new JSONObject();
    String sessionId = session.getId();
    Member memberInfo = memberDao.selectMemberInfo(sessionId);
    //소켓 종료
    if (rls.size() > 0) { //소켓이 종료되면 해당 세션값
      if (memberInfo == null) {
        log.info("already Deleted Member");
      } else if (rls.get(memberInfo.getSessionIdx()).get(session.getId()) != null) {
        HashMap<String, Object> map = rls.get(memberInfo.getSessionIdx());
        String roomId = memberInfo.getMemberRoomId();
        String memberId = memberInfo.getMemberId();
        Room roomInfo = roomDao.selectRoomInfo(roomId);
        int memberCount = roomInfo.getRoomCount();
        boolean isAdmin = memberInfo.getMemberAdminYN().equals("Y") ? true : false;
        String outMemberName = memberInfo.getMemberName();
        roomDao.decreaseRoomCount(roomId);
        memberDao.deleteMember(memberId);
        Member nextAdmin = memberDao.getNextAdmin(roomId);
        String roomStatus = StringUtils.isEmpty(roomInfo.getRoomStatus()) == true ? "wait" : roomInfo.getRoomStatus();
        if (nextAdmin != null) {
          String sessionKey = nextAdmin.getMemberId();
          WebSocketSession wss = (WebSocketSession) map.get(sessionKey);
          if (isAdmin && roomStatus.equals("wait")) {
            adminObj.put("type", "adminLeft");
            adminObj.put("isAdmin", true);
            memberDao.setMemberAdmin(memberId);
            wss.sendMessage(new TextMessage(adminObj.toJSONString()));
          }
          map.remove(session.getId());
          obj.put("type", "memberOut");
          obj.put("outMemberName", outMemberName);
          List<String> memberNames = memberDao.selectMemberNames(roomId);
          obj.put("memberList", memberNames);
          messageSend(roomId,obj);
          rls.set(memberInfo.getSessionIdx(), map);
        } else {
          if (!StringUtils.isEmpty(rls.get(memberInfo.getSessionIdx()).get(session.getId()))) {
            roomDao.deleteRoom(roomId);
            rls.set(memberInfo.getSessionIdx(),new HashMap<>());
          }
        }
      }
    }
    super.afterConnectionClosed(session, status);
  }

  public int getRoomCount(String roomId) {
    if (rls.size() > 0) {
      for (int i = 0; i < rls.size(); i++) {
        String getRoomId = (String) rls.get(i).get("roomId");
        if (getRoomId.equals(roomId)) {
          return (int) rls.get(i).get("memberCount");
        }
      }
    }
    return 0;
  }

  public HashMap<String, String> getMemberList(String roomId) {
    if (rls.size() > 0) {
      for (int i = 0; i < rls.size(); i++) {
        String getRoomId = (String) rls.get(i).get("roomId");
        if (getRoomId.equals(roomId)) {
          return (HashMap<String, String>) rls.get(i).get("memberList");
        }
      }
    }
    return new HashMap<>();
  }

  private static JSONObject jsonToObjectParser(String jsonStr) {
    JSONParser parser = new JSONParser();
    JSONObject obj = null;
    try {
      obj = (JSONObject) parser.parse(jsonStr);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return obj;
  }

  public static void setRoomId(String roomId) { //최초 생성하는 방이라면 방번호와 세션을 추가한다.
    HashMap<String, Object> map = new HashMap<>();
    map.put("roomId", roomId);
    map.put("memberList", new HashMap<>());
    map.put("memberCount", 0);
    map.put("roomStatus", "wait");
    rls.add(map);
  }

  private void messageSend(String jsonGetRoomId, JSONObject sendObj) {
    try {
      HashMap<String, Object> temp = new HashMap<>();
      Room roomInfo = roomDao.selectRoomInfo(jsonGetRoomId);
      temp = rls.get(roomInfo.getSessionIdx());

      Boolean voteGubun = sendObj.get("type").equals("voteStarted")           //투표 시작
          || sendObj.get("type").equals("mafiaVoteStarted");      //암살 시작

      if (voteGubun) {
        JSONObject zombieObj = new JSONObject(sendObj);
        zombieObj.put("type","zombie_" + zombieObj.get("type"));
        //해당 방의 세션들만 찾아서 메시지를 발송해준다.
        for (String k : temp.keySet()) {
          Boolean doNotSend = k.equals("roomId")          //방 고유 값
              || k.equals("adminSession")   //방장 아이디
              || k.equals("memberList")     //맴버 정보
              || k.equals("memberCount")    //방 인원 수
              || k.contains("_status")      //플레이어 상태 값
              || k.equals("voteStatus")      //플레이어 상태 값
              || k.equals("jobList")        //직업 리스트
              || k.equals("jobSessList")      //직업 리스트 sessionId
              || k.equals("roomStatus");    //방 상태 값

          if (doNotSend) { //방 정보 통과
            continue;
          }

          WebSocketSession wss = (WebSocketSession) temp.get(k);
          if (wss != null) {
            synchronized(wss) {
              wss.sendMessage(new TextMessage(sendObj.toJSONString()));
            }
          }
        }
      } else {
        //해당 방의 세션들만 찾아서 메시지를 발송해준다.
        for (String k : temp.keySet()) {
          Boolean doNotSend = k.equals("roomId")          //방 고유 값
              || k.equals("adminSession")   //방장 아이디
              || k.equals("memberList")     //맴버 정보
              || k.equals("memberCount")    //방 인원 수
              || k.contains("_status")      //플레이어 상태 값
              || k.equals("voteStatus")      //플레이어 상태 값
              || k.equals("jobList")        //직업 리스트
              || k.equals("jobSessList")      //직업 리스트 sessionId
              || k.equals("roomStatus");    //방 상태 값

          if (doNotSend) { //방 정보 통과
            continue;
          }

          WebSocketSession wss = (WebSocketSession) temp.get(k);
          if(wss != null) {
            synchronized(wss) {
              wss.sendMessage(new TextMessage(sendObj.toJSONString()));
            }
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public ArrayList<Map<String, String>> giveJobs(String roomId) {
    Map<String, String> jobs = new HashMap<>();
    int idx = 0;
    for (int i = 0; i < rls.size(); i++) {
      String getRoomId = (String) rls.get(i).get("roomId");
      if (getRoomId.equals(roomId)) {
        idx = i;
        break;
      }
    }
    Map<String, String> memberList = (Map<String, String>) rls.get(idx).get("memberList");
    int memberCnt = memberList.size();
    String[] specialJobs = new String[]{"mafia","mafia","cop","doctor"};
    ArrayList<Map<String, String>> jobList = new ArrayList<>();
    ArrayList<String> memberNameList = new ArrayList<>();

    for (String memberName : memberList.keySet()) {
      memberNameList.add(memberName);
    }
    Collections.shuffle(memberNameList);

    for (int i=0;i<4;i++) {
      Map<String, String> map = new HashMap<>();
      map.put("name",memberNameList.get(i));
      map.put("job",specialJobs[i]);
      jobList.add(map);
    }

    for (int i=4;i<memberCnt;i++) {
      Map<String, String> map = new HashMap<>();
      map.put("name",memberNameList.get(i));
      map.put("job","citizen");
      jobList.add(map);
    }

    return jobList;
  }

  public void getRandomStoreList(String roomId, Target parameterTarget) {
    List<Store> storeList = lunchEtcDao.selectStoreInfoList(parameterTarget);
    List<Store> suffleList = new ArrayList<>();
    List<Store> resultList = new ArrayList<>();
    int suffleListCount = 0;
    int suffleCount = (int) ((Math.random() * 10) + 1);

    for (Store tempStore : storeList) {
      for (int i = 0; i < tempStore.getStoreWeight(); i++) {
        suffleList.add(tempStore);
      }
    }

    for (int i = 0; i < suffleCount; i++) {
      Collections.shuffle(suffleList);
    }

    while (resultList.size() < 5) {
      if (!resultList.contains(suffleList.get(suffleListCount))) {
        resultList.add(suffleList.get(suffleListCount));
      }
      suffleListCount++;
    }

    ObjectMapper mapper = new ObjectMapper();
    JSONObject sendObj = new JSONObject();
    sendObj.put("type", "gameStart");
    try {
      sendObj.put("storeList", mapper.writeValueAsString(resultList));
    } catch (Exception e) {
      e.printStackTrace();
    }
    //sendObj.put("storeList", resultList);

    messageSend(roomId,sendObj);
  }

  public void getRandomStoreListExceptWeek(String roomId, String roomName) {
    Target parameterTarget = new Target(roomName);
    List<Store> storeList = lunchEtcDao.selectStoreInfoList(parameterTarget);
    List<Store> suffleList = new ArrayList<>();
    List<Store> resultList = new ArrayList<>();
    int suffleListCount = 0;
    int suffleCount = (int) ((Math.random() * 10) + 1);

    for (Store tempStore : storeList) {
      for (int i = 0; i < tempStore.getStoreWeight(); i++) {
        suffleList.add(tempStore);
      }
    }

    for (int i = 0; i < suffleCount; i++) {
      Collections.shuffle(suffleList);
    }

    while (resultList.size() < 5) {
      if (!resultList.contains(suffleList.get(suffleListCount))) {
        resultList.add(suffleList.get(suffleListCount));
      }
      suffleListCount++;
    }

    ObjectMapper mapper = new ObjectMapper();
    JSONObject sendObj = new JSONObject();
    sendObj.put("type", "gameStart");
    try {
      sendObj.put("storeList", mapper.writeValueAsString(resultList));
    } catch (Exception e) {
      e.printStackTrace();
    }
    //sendObj.put("storeList", resultList);

    messageSend(roomId,sendObj);
  }

  public void mafiaKill(String jsonGetRoomId, String storeName, int storeCount, String storeMenuUrl) {
    try {
      JSONObject sendObj = new JSONObject();
      sendObj.put("type", "mafiaKillComplete");
      sendObj.put("storeName", storeName);
      sendObj.put("storeCount", storeCount);
      sendObj.put("storeMenuUrl", storeMenuUrl);
      messageSend(jsonGetRoomId, sendObj);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void voteComplete(String jsonGetRoomId, int voteCount, String memberId) {
    try {
      JSONObject sendObj = new JSONObject();
      sendObj.put("type", "voteComplete");
      sendObj.put("voteCount", voteCount);
      sendObj.put("memberId", memberId);
      messageSend(jsonGetRoomId, sendObj);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}