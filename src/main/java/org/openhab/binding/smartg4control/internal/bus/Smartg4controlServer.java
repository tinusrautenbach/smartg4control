/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartg4control.internal.bus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author TinusRautenbach - Initial contribution
 */
public class Smartg4controlServer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Smartg4controlServer.class);

    private static Smartg4controlServer instance = null;

    public static Smartg4controlServer getInstance(String listenAddress, String gateWayAddress) {

        if (instance == null) {
            instance = new Smartg4controlServer();
            try {
                instance.start(listenAddress, gateWayAddress);
            } catch (IOException e) {

                logger.error("cant start server", e);
            }

        } else { // check if socket created etc.

            if (!instance.isrunning()) {
                logger.debug("server not running trying to start");
                try {
                    instance.start(listenAddress, gateWayAddress);
                } catch (IOException e) {

                    logger.error("cant start server", e);
                }
            }

        }

        return instance;

    }

    // The protocol defines 27 header bytes + up to 128 aux data bytes.
    // However, binary representation allows up 256 bytes of aux data, so
    // use 512 for buffer size just in case.
    public static final int MAX_PACKET_SIZE = 512;

    public static final int PORT = 6000;

    Thread serverThread;
    private volatile boolean running = true;


    DatagramSocket socket;
    // MulticastSocket socket;

    ConcurrentHashMap<String, ISmartg4controlDevice> devices = new ConcurrentHashMap<String, ISmartg4controlDevice>();
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    InetAddress gatewayAddress;
    InetAddress listenAddress;

    int retryCount = 3;
    int retryTime = 600;
    TimeUnit retryTimeUnit = TimeUnit.MILLISECONDS;
    Timer timer = new Timer();

    public boolean isrunning() {
        return (socket != null && serverThread != null);

    }
    public void terminate() {
        running = false;
    }

    public synchronized void start(String listenAddress, String gatewayAddress) throws IOException {
        if (socket != null || serverThread != null) {
            throw new IOException("server already started");
        }
        this.blist = listAllBroadcastAddresses();

        this.gatewayAddress = InetAddress.getByName(gatewayAddress);
        this.listenAddress = InetAddress.getByName(listenAddress);
        logger.debug("server start {}", listenAddress, PORT);

        // MulticastSocket ms = new MulticastSocket(PORT);
        // socket = ms;
        socket = new DatagramSocket(PORT);
        InetAddress group = InetAddress.getByName("230.0.0.0");

        socket.setBroadcast(true);
        socket.setReuseAddress(true);
        // socket.joinGroup(group);

        // socket.bind( (new InetSocketAddress(this.listenAddress, PORT));

        serverThread = new Thread(this);
        serverThread.start();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                for (Map.Entry<String, ISmartg4controlDevice> entry : devices.entrySet()) {
                    entry.getValue().updateSensor();
                }

            }
        };

        long delay = 0;
        long intevalPeriod = 60 * 1000;
        // schedules the task to be run in an interval

        if (timer == null) {
            timer = new Timer();

        }
        timer.scheduleAtFixedRate(task, delay, intevalPeriod);

    }

    public synchronized void stop() {
        if (socket == null || serverThread == null) {
            return;
        }

        socket.close();

            terminate() ;

            
  

        serverThread = null;
        socket = null;
        timer.cancel();
        timer = null;

    }

    @Override
    public void run() {
        byte[] recvBuf = new byte[MAX_PACKET_SIZE];
        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);

        while (running) {
            try {
                socket.receive(packet);
            } catch (SocketException e) {
                logger.error("Socket error", e);
                break;
            } catch (IOException e) {
                logger.warn("io error");
                continue;
            }

            Smartg4controlPacket p = Smartg4controlPacket.parse(packet.getData(), packet.getLength());
            // logger.debug("packet {} {} {} {}", p, p.getSourceDevice(), Integer.toHexString(p.getCommand()),
            // Smartg4controlDDP.CMD_DDP_READ_TEMP_RESPONSE);

            if (p.getCommand() == Smartg4controlDimmer.CMD_DIMMER_RESPONSE_STATUS_OF_CHANNELS) {

                byte[] data = p.getData();
                int qtyChannels = data[0];

                for (int i = 1; i <= qtyChannels; i++) {
                    String tAddress = p.getSourceSubnetID() + ":" + p.getSourceDevideID() + "-" + i;
                    ISmartg4controlDevice d = devices.get(tAddress);
                    if (d != null) {

                        d.processPacket(p);

                    }

                }

            } else if (p.getCommand() == Smartg4controlDimmer.CMD_DIMMER_SET_STATE_RESPONSE) {
                if (p.getSourceDevice() == Smartg4controlDimmer.DEVICE_ID_DIMMER_8
                        || p.getSourceDevice() == Smartg4controlDimmer.DEVICE_ID_RELAY_12
                        || p.getSourceDevice() == Smartg4controlDimmer.DEVICE_ID_RELAY_6
                        || p.getSourceDevice() == Smartg4controlDimmer.DEVICE_ID_RELAY_8) {

                    int channel = p.getData()[0];

                    String uAddress = p.getSourceSubnetID() + ":" + p.getSourceDevideID() + "-" + channel;

                    // logger.debug("packet {} {}", uAddress, p);

                    if (p != null) {

                        ISmartg4controlDevice d = devices.get(uAddress);

                        if (d != null) {
                            d.processPacket(p);
                        }
                    }

                }
            } else if (p.getCommand() == Smartg4controlSensor.CMD_READ_TEMP_RESPONSE) {
                if (p.getSourceDevice() == Smartg4controlSensor.DEVICE_ID_8IN1 || p.getSourceDevice() == Smartg4controlSensor.DEVICE_ID_5IN1
                        || p.getSourceDevice() == Smartg4controlSensor.DEVICE_ID_6IN1
                        || p.getSourceDevice() == Smartg4controlSensor.DEVICE_ID_9IN1) {
                    // logger.debug("process TEMP RESPONSE");
                    String uAddress = p.getSourceSubnetID() + ":" + p.getSourceDevideID();
                    if (p != null) {

                        ISmartg4controlDevice d = devices.get(uAddress);

                        if (d != null) {
                            d.processPacket(p);
                        }
                    }

                }
            } else if (p.getCommand() == Smartg4controlLogic.CMD_DATE_TIME_RESPONSE) {

                logger.debug("process logic time RESPONSE");
                String uAddress = p.getSourceSubnetID() + ":" + p.getSourceDevideID();
                if (p != null) {

                    ISmartg4controlDevice d = devices.get(uAddress);

                    if (d != null) {
                        d.processPacket(p);
                    }
                }

            } else if (p.getCommand() == Smartg4controlDDP.CMD_DDP_READ_TEMP_RESPONSE) {

                logger.debug("process DDP TEMP RESPONSE");
                String uAddress = p.getSourceSubnetID() + ":" + p.getSourceDevideID();
                if (p != null) {

                    ISmartg4controlDevice d = devices.get(uAddress);

                    if (d != null) {
                        d.processPacket(p);
                    }
                }

            } else if (p.getCommand() == GenericSmartg4controlDevice.CMD_DETECT_ADDRESS_RESPONSE) {

                logger.debug("process mac response");
                String uAddress = p.getSourceSubnetID() + ":" + p.getSourceDevideID();
                if (p != null) {

                    logger.debug("mac response {}", p);
                }

            } else if (p.getCommand() == Smartg4controlDimmer.CMD_FORWARDLY_REPORT_DIMMER_STATUS) {

                // logger.debug("forward report status of dimmer");
                // String uAddress = p.getSourceSubnetID() + ":" + p.getSourceDevideID();
                // TODO: do nothing at the moment

            } else if (p.getCommand() == Smartg4controlDimmer.CMD_DIMMER_SET_STATE) {

                // setting level from other switch

            }

            else {
                logger.debug("other {}", p);
            }
        }

    }

    public void addDevice(ISmartg4controlDevice device) {
        logger.debug("hs.addDevice {}", device.getAddress());
        devices.put(device.getAddress(), device);
    }

    public ISmartg4controlDevice getDevice(String address) {
        return devices.get(address);
    }

    List<InetAddress> listAllBroadcastAddresses() throws SocketException {
        List<InetAddress> broadcastList = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            networkInterface.getInterfaceAddresses().stream().map(a -> a.getBroadcast()).filter(Objects::nonNull)
                    .forEach(broadcastList::add);
        }
        return broadcastList;
    }

    List<InetAddress> blist;

    public void sendPacket(Smartg4controlPacket p) throws IOException {
        DatagramSocket s = socket;

        if (s == null) {
            throw new IOException("server not started");
        }
        p.setReplyAddress(listenAddress);
        p.setSourceAddress(0x01fe);

        byte[] bytes = p.getBytes();
        // logger.debug("send {} {} {} {} ", bytes, this.gatewayAddress, PORT, listenAddress);
        InetAddress group = InetAddress.getByName("255.255.255.255");

        for (InetAddress b : blist) {
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, b, PORT);
            // byte ttl = (byte) 1;
            // s.setTimeToLive(1);
            s.send(packet);
        }

    }

    public ScheduledFuture sendPacketWithRetry(Smartg4controlPacket p) throws IOException {
        return sendPacketWithRetry(p, retryCount, retryTime, retryTimeUnit);
    }

    private ScheduledFuture sendPacketWithRetry(Smartg4controlPacket p, int retryCount, int retryTime, TimeUnit timeUnit)
            throws IOException {
        sendPacket(p);
        PacketResender r = new PacketResender(p, this, retryCount);
        ScheduledFuture f = scheduler.scheduleAtFixedRate(r, retryTime, retryTime, timeUnit);
        r.setFuture(f);
        return f;
    }

}

/**
 *
 * @author TinusRautenbach - Initial contribution
 */
class PacketResender implements Runnable {
    private int retryCount;
    private Smartg4controlServer server;
    private Smartg4controlPacket packet;
    private ScheduledFuture future;

    private static final Logger logger = LoggerFactory.getLogger(PacketResender.class);

    public PacketResender(Smartg4controlPacket p, Smartg4controlServer s, int retryCount) {
        this.packet = p;
        this.server = s;
        this.retryCount = retryCount;
    }

    public void setFuture(ScheduledFuture future) {
        this.future = future;
    }

    @Override
    public void run() {
        if (retryCount == 0) {
            if (future != null) {
                future.cancel(true);
                future = null;
            }
            return;
        }
        retryCount--;
        try {
            server.sendPacket(packet);
        } catch (IOException e) {
            future.cancel(true);
            future = null;
        }
        logger.debug("Resending, {} retries left", retryCount);
    }
}
