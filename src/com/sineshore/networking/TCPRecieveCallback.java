package com.sineshore.networking;

import java.io.InputStream;
import java.io.OutputStream;

public interface TCPRecieveCallback {

    public abstract void recieve(String address, int port, InputStream input, OutputStream output);

}
