package com.test.demo.domain.log;

/**
 * Created by Ryan Miao on 12/14/17.
 */
public enum  SystemEvent {

    FIND_ONE_ROOM_FAILED(10001, "Find one room by id failed."),
    FIND_ONE_ROOM_NOT_EXIST(10002, "The room is not exist."),
    SAVE_ONE_ROOM_FAILED(10003, "Save a room failed."),
    UPDATE_ONE_ROOM_FAILED(10004, "Update a room failed."),
    FIND_ALL_ROOMS_FAILED(10005, "Find all rooms failed."),
    DELETE_ONE_ROOM_FAILED(10006, "Delete a room failed."),
    ;


    private final int id;
    private final String detail;


    SystemEvent(int i, String s) {
        this.id = i;
        this.detail = s;
    }

    public int getId() {
        return id;
    }

    public String getDetail() {
        return detail;
    }
}
