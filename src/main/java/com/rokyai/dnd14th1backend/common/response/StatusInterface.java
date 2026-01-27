package com.rokyai.dnd14th1backend.common.response;

public interface StatusInterface {
    int getHttpStatusCode();

    int getCustomStatusCode();

    String getDescription();
}
