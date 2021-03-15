package com.example.mafia.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import lombok.Data;

@Data
public class Store implements Serializable {
  String storeCode;
  String storeName;
  int storeWeight;
  int storeWeightCount;
  String storeNation;
  String storeKind1;
  String storeKind2;
  String storeKind3;
  String storeDeliveryYN;
  String storePaycoYN;
  String storeComment;
  String storeNaver;
  String regDttm;
}
