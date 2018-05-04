package com.semisonfire.cloudgallery.data.remote.response;

import com.google.gson.annotations.SerializedName;

public class Trash {

    @SerializedName("_embedded")
    private TrashResponse trashResponse;

    public TrashResponse getTrashResponse() {
        return trashResponse;
    }

    public void setTrashResponse(TrashResponse trashResponse) {
        this.trashResponse = trashResponse;
    }
}
