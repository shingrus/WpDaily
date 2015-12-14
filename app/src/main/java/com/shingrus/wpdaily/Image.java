package com.shingrus.wpdaily;

public final class Image {

    private final byte[] data;
    private final String url;
    private final Integer insertedAt;


    private final String provider;


    public final Integer getInsertedAt() {
        return insertedAt;
    }

    public final String getUrl() {
        return url;
    }

    public final byte[] getData() {
        return data;
    }

    public final String getProvider() {
        return provider;
    }


    public Image(String url, Integer insertedAt, String provider, byte[] data) {
        this.data = data;
        this.url = url;
        this.insertedAt = insertedAt;
        this.provider = provider;
    }
}
