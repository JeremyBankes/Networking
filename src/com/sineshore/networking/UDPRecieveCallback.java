package com.sineshore.networking;

public interface UDPRecieveCallback {

    public abstract void recieve(String address, int port, byte[] data);

}
