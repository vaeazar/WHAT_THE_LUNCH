package com.example.mafia.config;

import com.example.mafia.dao.LunchMemberDao;
import com.example.mafia.dao.LunchRoomDao;
import com.example.mafia.dao.MemberDao;
import com.example.mafia.dao.RoomDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShutdownEventListener implements ApplicationListener<ContextClosedEvent> {

  @Autowired
  RoomDao roomDao;

  @Autowired
  LunchRoomDao lunchRoomDao;

  @Autowired
  MemberDao memberDao;

  @Autowired
  LunchMemberDao lunchMemberDao;

  @Override
  public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
    log.info("============================");
    log.info("System shutting down!");
    roomDao.deleteAllRoom();
    lunchRoomDao.deleteAllRoom();
    log.info("Room Delete complete");
    memberDao.deleteAllMember();
    lunchMemberDao.deleteAllMember();
    log.info("Member Delete complete");
    log.info("============================");
  }
}
