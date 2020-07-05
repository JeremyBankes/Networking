package com.jeremy.networking;

import java.net.InetAddress;

public class Endpoint {

	public final InetAddress address;
	public final int port;

	public Endpoint(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
		if (!(object instanceof Endpoint)) return false;
		Endpoint endpoint = (Endpoint) object;
		return address.equals(endpoint.address) && port == endpoint.port;
	}

	@Override
	public int hashCode() {
		int hashCode = port;
		boolean flip;
		for (byte byteValue : address.getAddress()) {
			flip = (byteValue * port) % 2 == 0;
			if (flip) hashCode *= byteValue;
			else hashCode -= (port * byteValue) * 31;
			flip = !flip;
		}
		return hashCode;
	}

	@Override
	public String toString() {
		return address.getHostAddress() + ":" + port;
	}

}
