package com.allobank.splitbill.dto;

import java.util.List;

public record CreateBillGroupRequest(String name, List<String> participants) {}
