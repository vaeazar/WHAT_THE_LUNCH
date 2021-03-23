package com.example.mafia.controller;

import com.example.mafia.dao.LunchEtcDao;
import com.example.mafia.dao.MemberDao;
import com.example.mafia.dao.RoomDao;
import com.example.mafia.domain.Member;
import com.example.mafia.domain.Room;
import com.example.mafia.domain.Store;
import com.example.mafia.domain.Target;
import com.example.mafia.domain.VisitLog;
import com.example.mafia.handler.SocketHandler;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class MainController {

  List<Room> roomArrayList = new ArrayList<Room>();
  static int roomNumber = 0;

  @Autowired
  SocketHandler socketHandler;

  @Autowired
  RoomDao roomDao;

  @Autowired
  MemberDao memberDao;

  @Autowired
  LunchEtcDao lunchEtcDao;

  @RequestMapping("/")
  public ModelAndView Index() {
    ModelAndView mv = new ModelAndView();
    mv.setViewName("room");
    return mv;
  }

  /**
   * 방 페이지
   * @return
   */
  @RequestMapping("/room")
  public ModelAndView room() {
    ModelAndView mv = new ModelAndView();
    mv.setViewName("room");
    return mv;
  }

//  /**
//   * 방 생성하기
//   * @param params
//   * @return
//   */
//  @RequestMapping("/createRoom")
//  public @ResponseBody
//  List<Room> createRoom(@RequestParam HashMap<Object, Object> params){
//    String roomName = (String) params.get("roomName");
//    if(roomName != null && !roomName.trim().equals("")) {
//      Room room = new Room();
//      room.setRoomNumber(++roomNumber);
//      room.setRoomName(roomName);
//      room.setRoomCount(0);
//      room.setRoomStatus(true);
//      String uniqueValue = UUID.randomUUID().toString().substring(0,8);
//      uniqueValue += System.currentTimeMillis();
//      room.setRoomId(uniqueValue);
//      SocketHandler.setRoomId(uniqueValue);
//      roomList.add(room);
//    }
//    return roomList;
//  }

  /**
   * 방 생성하기
   * @param params
   * @return
   */
  @RequestMapping("/createRoom")
  public ModelAndView createRoom(@RequestParam HashMap<Object, Object> params, HttpServletRequest request) {
    ModelAndView mv = new ModelAndView();
    String roomName = (String) params.get("roomName");
    String url = request.getHeader("referer");
    if (url == null) {
      mv.addObject("errorFlag","deletedRoom");
      mv.addObject("errorMessage", "비정상적인 접근입니다.");
      mv.setViewName("room");
      return mv;
    } else if (ObjectUtils.isEmpty(params.get("userId"))) {
      mv.addObject("errorFlag","deletedRoom");
      mv.addObject("errorMessage", "아이디를 입력해주세요.");
      mv.setViewName("room");
      return mv;
    }
    if(roomName != null && !roomName.trim().equals("")) {
      Room room = new Room();
      room.setRoomNumber(0);
      room.setRoomName(roomName);
      room.setRoomCount(0);
      room.setRoomStatus("wait");
      String uniqueValue = UUID.randomUUID().toString().substring(0,8);
      uniqueValue += System.currentTimeMillis();
      room.setRoomId(uniqueValue);
      SocketHandler.setRoomId(uniqueValue);

      roomDao.insert(room);

      roomArrayList.add(room);
      mv.addObject("roomName", params.get("roomName"));
      mv.addObject("roomNumber", roomNumber);
      mv.addObject("roomId", uniqueValue);
      mv.addObject("userId", params.get("userId"));
      mv.setViewName("mafiaChat");
    } else {
      mv.addObject("errorFlag","deletedRoom");
      mv.addObject("errorMessage", "존재하지 않는 방입니다.");
      mv.setViewName("room");
    }
    return mv;
  }

  /**
   * 방 정보가져오기
   * @param params
   * @return
   */
  @RequestMapping("/getRoom")
  public @ResponseBody List<Room> getRoom(@RequestParam HashMap<Object, Object> params){
    return roomDao.selectRoomInfoList();
  }

//  /**
//   * 방 정보가져오기
//   * @param params
//   * @return
//   */
//  @RequestMapping("/getRoom")
//  public @ResponseBody List<Room> getRoom(@RequestParam HashMap<Object, Object> params){
//    List <Room> sendList = new ArrayList<>();
//    for (int i = 0; i < roomList.size(); i++) {
//      Room room = roomList.get(i);
//      room.setRoomCount(socketHandler.getRoomCount(room.getRoomId()));
//      roomList.set(i,room);
//      if (socketHandler.getRoomCount(room.getRoomId()) > 0) {
//        sendList.add(room);
//      }
//    }
//    return sendList;
//  }

  /**
   * 방 시작
   * @param params
   * @return
   */
  @RequestMapping("/setRoomStart")
  public @ResponseBody void setRoomStart(@RequestParam HashMap<Object, Object> params){
    HashMap<String, String> jobs = new HashMap<>();
    List<String> parameterNationAndKind = new ArrayList<>();
    String roomId = String.valueOf(params.get("roomId"));
    String roomName = String.valueOf(params.get("roomName"));
    String weekExcept = String.valueOf(params.get("weekExcept"));
    String nation = String.valueOf(params.get("nation"));
    String kind = String.valueOf(params.get("kind"));

    String[] nationSplit = nation.split("|");
    String[] kindSplit = kind.split("|");

    for (String tempNation : nationSplit) {
      parameterNationAndKind.add(tempNation);
    }
    for (String tempKind : kindSplit) {
      parameterNationAndKind.add(tempKind);
    }

    Room startedRoom = roomDao.selectRoomInfo(roomId);
    Target parameterTarget = new Target(weekExcept);
    parameterTarget.setRoomName(roomName);
    parameterTarget.setNationAndKinds(parameterNationAndKind);
    int sessionIdx = startedRoom.getSessionIdx();
    if (!ObjectUtils.isEmpty(startedRoom)) {
      startedRoom.setRoomStatus("start");
      HashMap<String, Integer> votes = new HashMap<>();
      roomArrayList.get(sessionIdx).setVotes(votes);
      roomArrayList.get(sessionIdx).setVoteCount(0);
      socketHandler.getRandomStoreList(roomId, parameterTarget);
      roomDao.changeRoomStatus(startedRoom);
    }
  }

  /**
   * 방 시작
   * @param params
   * @return
   */
  @RequestMapping("/getMafiaList")
  public @ResponseBody void getMafiaList(@RequestParam HashMap<Object, Object> params){
    for (int i = 0; i < roomArrayList.size(); i++) {
      Room room = roomArrayList.get(i);
      if (room.getMafia() == null) room.setMafia(new ArrayList<>());
      if (room.getRoomId().equals(params.get("roomId"))) {
        room.setRoomStatus("start");
        List<String> mafiaList = room.getMafia();
        mafiaList.add(String.valueOf(params.get("userId")));
        roomArrayList.set(i,room);
      }
    }
  }

//  /**
//   * 방 제거
//   * @param params
//   * @return
//   */
//  @RequestMapping("/delRoom")
//  public @ResponseBody void delRoom(@RequestParam HashMap<Object, Object> params){
//    for (int i = 0; i < roomList.size(); i++) {
//      Room room = roomList.get(i);
//      if (room.getRoomId().equals(params.get("roomId"))) {
//        roomList.remove(i);
//      }
//    }
//  }

  /**
   * 채팅방
   * @return
   */
  @RequestMapping("/moveChating")
  public ModelAndView chating(@RequestParam HashMap<Object, Object> params, HttpServletRequest request) {
    ModelAndView mv = new ModelAndView();
    String url = request.getHeader("referer");
    if (url == null) {
      mv.addObject("errorFlag","deletedRoom");
      mv.addObject("errorMessage", "비정상적인 접근입니다.");
      mv.setViewName("room");
      return mv;
    } else if (ObjectUtils.isEmpty(params.get("userId"))) {
      mv.addObject("errorFlag","deletedRoom");
      mv.addObject("errorMessage", "아이디를 입력해주세요.");
      mv.setViewName("room");
      return mv;
    }
    int roomNumber = Integer.parseInt((String) params.get("roomNumber"));
    String roomId = params.get("roomId").toString();

    Room existChk = roomDao.selectRoomInfo(roomId);
    //List<Room> new_list = roomArrayList.stream().filter(o->o.getRoomNumber()==roomNumber).collect(Collectors.toList());
    if(existChk != null) {
      mv.addObject("roomName", existChk.getRoomName());
      mv.addObject("roomNumber", existChk.getRoomNumber());
      mv.addObject("roomId", existChk.getRoomId());
      mv.addObject("userId", params.get("userId"));
      mv.setViewName("mafiaChat");
    }else {
      mv.addObject("errorFlag","deletedRoom");
      mv.addObject("errorMessage", "존재하지 않는 방입니다.");
      mv.setViewName("room");
    }
    return mv;
  }

  @RequestMapping("/roomDelete/{id}")
  public @ResponseBody
  String roomDelete(@PathVariable("id") String roomId) {
    try {
      for(int i=0; i< roomArrayList.size(); i++) {
        String getRoomId = roomArrayList.get(i).getRoomId();
        if (!StringUtils.isEmpty(getRoomId) && getRoomId.equals(roomId)) {
          roomArrayList.remove(i);
          break;
        }
      }
      return "deleteComplete";
    } catch (Exception e) {
      return "deleteFail";
    }
  }

  @RequestMapping("/startGame/{id}")
  public @ResponseBody
  ArrayList<Map<String, String>> startGame(@PathVariable("id") String roomId) {
    ArrayList<Map<String, String>> jobs = new ArrayList<>();
    for (int i=0;i< roomArrayList.size();i++) {
      String getRoomId = roomArrayList.get(i).getRoomId();
      if (!StringUtils.isEmpty(getRoomId) && getRoomId.equals(roomId)) {
        jobs = socketHandler.giveJobs(roomId);
      }
    }
    return jobs;
  }

  @RequestMapping("/getMemberNames")
  public @ResponseBody
  String getMemberNames(@RequestParam HashMap<Object, Object> params) {
    String roomId = StringUtils.isEmpty(params.get("roomId")) == true ? "" : params.get("roomId").toString();
    JSONObject memberListString = new JSONObject();

    if (roomId.equals("")) {
      return memberListString.toJSONString();
    }
    List<String> memberList = memberDao.selectAliveMemberNames(roomId);
    try {
      if (memberList == null) memberList = new ArrayList<>();
      memberListString.put("memberList",memberList);
      return memberListString.toJSONString();
    } catch (Exception e) {
      memberListString.put("memberList",new ArrayList<>());
      return memberListString.toJSONString();
    }
  }

  @RequestMapping("/getCivilNames")
  public @ResponseBody
  String getCivilNames(@RequestParam HashMap<Object, Object> params) {
    String roomId = StringUtils.isEmpty(params.get("roomId")) == true ? "" : params.get("roomId").toString();
    JSONObject memberListString = new JSONObject();

    if (roomId.equals("")) {
      return memberListString.toJSONString();
    }
    List<String> memberList = memberDao.selectCivil(roomId);

    try {
      if (memberList == null) memberList = new ArrayList<>();
      memberListString.put("civilList",memberList);
      return memberListString.toJSONString();
    } catch (Exception e) {
      memberListString.put("civilList",new ArrayList<>());
      return memberListString.toJSONString();
    }
  }

  @RequestMapping("/voteStart")
  public @ResponseBody
  void VoteStart(@RequestParam HashMap<Object, Object> params) {
    String roomId = StringUtils.isEmpty(params.get("roomId")) == true ? "" : params.get("roomId").toString();
    int sessionIdx = roomDao.selectRoomInfo(roomId).getSessionIdx();
    roomArrayList.get(sessionIdx).setVoteCount(0);
    roomArrayList.get(sessionIdx).setVotes(new HashMap<>());
  }

  @RequestMapping("/clickStore")
  public @ResponseBody
  String ClickStore(@RequestParam HashMap<Object, Object> params) {
    String roomId = StringUtils.isEmpty(params.get("roomId")) == true ? "" : params.get("roomId").toString();
    String memberId = StringUtils.isEmpty(params.get("memberId")) == true ? "" : params.get("memberId").toString();
    String storeName = StringUtils.isEmpty(params.get("storeName")) == true ? "" : params.get("storeName").toString();

    try {
      for(int i=0; i< roomArrayList.size(); i++) {
        String getRoomId = roomArrayList.get(i).getRoomId();
        if (!StringUtils.isEmpty(getRoomId) && getRoomId.equals(roomId)) {
          HashMap<String, Integer> votes = roomArrayList.get(i).getVotes();
          int voteCount = roomArrayList.get(i).getVoteCount() + 1;
          if (CollectionUtils.isEmpty(votes)) {
            votes = new HashMap<>();
            votes.put(storeName,1);
          } else if (votes.get(storeName) == null){
            votes.put(storeName,1);
          } else {
            votes.put(storeName, votes.get(storeName) + 1);
          }
          roomArrayList.get(i).setVotes(votes);
          roomArrayList.get(i).setVoteCount(voteCount);
          socketHandler.voteComplete(roomId,voteCount,memberId);
          break;
        }
      }
      return "voteComplete";
    } catch (Exception e) {
      return "voteFail";
    }
  }

  @RequestMapping("/mafiaKill")
  public @ResponseBody
  String mafiaKill(@RequestParam HashMap<Object, Object> params) {
    String roomId = StringUtils.isEmpty(params.get("roomId")) == true ? "" : params.get("roomId").toString();
    try {
      int storeCount = 0;
      ArrayList<String> equalList = new ArrayList<>();
      int equalIndex = 0;
      String storeName = "";
      Boolean resultEqual = true;
      for(int i=0; i< roomArrayList.size(); i++) {
        String getRoomId = roomArrayList.get(i).getRoomId();
        String getRoomName = roomArrayList.get(i).getRoomName();

        if (!StringUtils.isEmpty(getRoomId) && getRoomId.equals(roomId)) {
          HashMap<String, Integer> votes = roomArrayList.get(i).getVotes();
          if (CollectionUtils.isEmpty(votes)) {
            socketHandler.mafiaKill(roomId, storeName, storeCount, "");
            break;
          }
          Iterator<String> keys = votes.keySet().iterator();
          while( keys.hasNext() ){
            String key = keys.next();
            if (storeCount < votes.get(key)) {
              storeCount = votes.get(key);
              storeName = key;
              resultEqual = false;
              if (equalIndex != 0) {
                equalList = new ArrayList<>();
                equalIndex = 0;
              }
            } else if (storeCount == votes.get(key)) {
              equalList.add(equalIndex,key);
              equalIndex++;
              resultEqual = true;
            }
          }

          if (resultEqual) {
            equalList.add(storeName);
            Collections.shuffle(equalList);
            storeName = equalList.get(0);
          }

          Store parameterStore = lunchEtcDao.selectStoreInfoByName(storeName);
          Calendar cal = Calendar.getInstance();
          int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
          VisitLog visitLog = new VisitLog();
          visitLog.setDayOfWeek(dayOfWeek);
          visitLog.setStoreInfo(parameterStore);
          visitLog.setVisitTeam(getRoomName);
          lunchEtcDao.insertVisitLog(visitLog);
          socketHandler.mafiaKill(roomId, storeName, storeCount, parameterStore.getStoreMenuUrl());
          votes = new HashMap<>();
          roomArrayList.get(i).setVotes(votes);
          break;
        }
      }
      return "executed";
    } catch (Exception e) {
      return "executeFail";
    }
  }

  @RequestMapping("/mafiaVote")
  public @ResponseBody
  String mafiaVote(@RequestParam HashMap<Object, Object> params) {
    String roomId = StringUtils.isEmpty(params.get("roomId")) == true ? "" : params.get("roomId").toString();
    String playerId = StringUtils.isEmpty(params.get("playerId")) == true ? "" : params.get("playerId").toString();

    try {
      for(int i=0; i< roomArrayList.size(); i++) {
        String getRoomId = roomArrayList.get(i).getRoomId();
        if (!StringUtils.isEmpty(getRoomId) && getRoomId.equals(roomId)) {
          HashMap<String, Integer> votes = roomArrayList.get(i).getVotes();
          if (CollectionUtils.isEmpty(votes)) {
            votes = new HashMap<>();
            votes.put(playerId,1);
          } else if (votes.get(playerId) == null){
            votes.put(playerId,1);
          } else {
            votes.put(playerId, votes.get(playerId) + 1);
          }
          roomArrayList.get(i).setVotes(votes);
          break;
        }
      }
      return "mafiaVoteComplete";
    } catch (Exception e) {
      return "mafiaVoteFail";
    }
  }
}
