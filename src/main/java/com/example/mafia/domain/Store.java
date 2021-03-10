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
  String storeKind;
  String storeDeliveryYN;
  String storePaycoYN;
  String storeComment;
  String regDttm;
}
