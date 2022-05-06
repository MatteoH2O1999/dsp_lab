package lez04;

import org.eclipse.paho.client.mqttv3.*;

import java.io.IOException;
import java.util.*;

public class MqttMainProcess {
    public static void main(String[] args) throws IOException {
        String broker = "tcp://localhost:1883";

        TemperatureSensor sensor1 = new TemperatureSensor(broker, "1", 2);
        TemperatureSensor sensor2 = new TemperatureSensor(broker, "2", 2);
        TemperatureSensor sensor3 = new TemperatureSensor(broker, "3", 2);

        HeatersManager heatersManager = new HeatersManager(broker, "4", 2);

        Heaters heaters = new Heaters(broker, "5", 2);

        sensor1.start();
        sensor2.start();
        sensor3.start();
        heatersManager.start();
        heaters.start();

        System.out.println("Press any key to stop...");
        System.in.read();

        sensor1.shutdown();
        sensor2.shutdown();
        sensor3.shutdown();
        heatersManager.shutdown();
        heaters.shutdown();
    }
}

class TemperatureSensor extends Thread {
    final String topic = "home/sensors/temp";
    final SensorSwitch status = new SensorSwitch(true);
    private final Random randomGenerator = new Random();
    final String brokerAddress, clientId;
    private MqttClient mqttClient;
    final int qos;

    public TemperatureSensor(String brokerAddress, String clientId, int qos) {
        if (qos > 2 || qos < 0) {
            throw new IllegalArgumentException();
        }
        this.brokerAddress = brokerAddress;
        this.clientId = clientId;
        this.qos = qos;
    }

    @Override
    public void run() {
        try {
            this.connect();
        } catch (MqttException e) {
            e.printStackTrace();
            return;
        }
        int temperature;
        while (this.status.isOn()) {
            try {
                temperature = this.getTemperature(18, 22);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            try {
                this.sendTemperature(temperature);
            } catch (MqttException e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            this.mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void connect() throws MqttException {
        this.mqttClient = new MqttClient(this.brokerAddress, this.clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setWill(this.topic, "Shutdown".getBytes(), 1, false);
        options.setKeepAliveInterval(60);
        this.mqttClient.connect(options);
    }

    private int getTemperature(int lowerBound, int upperBound) throws InterruptedException {
        Thread.sleep(5000);
        return this.randomGenerator.nextInt(upperBound - lowerBound) + lowerBound;
    }

    private void sendTemperature(int temperature) throws MqttException {
        MqttMessage message = new MqttMessage(String.valueOf(temperature).getBytes());
        message.setQos(1);
        message.setRetained(false);
        this.mqttClient.publish(this.topic, message);
    }

    public void shutdown() {
        this.status.shutdown();
    }
}

class HeatersManager extends Thread {
    final String sensorTopic = "home/sensors/temp";
    final String heaterTopic = "home/controllers/temp";
    final SensorSwitch status = new SensorSwitch(true);
    final String brokerAddress, clientId;
    private MqttClient mqttClient;
    final int qos;
    final int[] temperatures = new int[5];

    public HeatersManager(String brokerAddress, String clientId, int qos) {
        if (qos > 2 || qos < 0) {
            throw new IllegalArgumentException();
        }
        this.brokerAddress = brokerAddress;
        this.clientId = clientId;
        this.qos = qos;
    }

    @Override
    public void run() {
        try {
            this.connect();
            this.subscribe();
        } catch (MqttException e) {
            e.printStackTrace();
            return;
        }
        while (this.status.isOn()) {
            try {
                this.status.waitForShutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            this.mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void connect() throws MqttException {
        this.mqttClient = new MqttClient(this.brokerAddress, this.clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setWill(this.heaterTopic, "Shutdown".getBytes(), 1, false);
        options.setKeepAliveInterval(60);
        this.mqttClient.connect(options);
    }

    private void subscribe() throws MqttException {
        HeatersManager currentInstance = this;
        this.mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                cause.printStackTrace();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                int temperature = Integer.parseInt(new String(message.getPayload()));
                currentInstance.addTemperature(temperature);
                float currentAvg = currentInstance.getAverageTemperature();
                currentInstance.sendCommand(currentAvg > 20);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
        this.mqttClient.subscribe(this.sensorTopic, this.qos);
    }

    private void sendCommand(boolean heaterOn) throws MqttException {
        MqttMessage message = new MqttMessage(String.valueOf(heaterOn).getBytes());
        message.setRetained(false);
        message.setQos(this.qos);
        this.mqttClient.publish(this.heaterTopic, message);
    }

    private synchronized void addTemperature(int temperature) {
        System.arraycopy(this.temperatures, 0, this.temperatures, 1, this.temperatures.length - 1);
        this.temperatures[0] = temperature;
    }

    private synchronized float getAverageTemperature() {
        float avg = 0;
        for (int temperature : this.temperatures) {
            avg += temperature;
        }
        return avg / this.temperatures.length;
    }

    public void shutdown() {
        this.status.shutdown();
    }
}

class Heaters extends Thread {
    final String heaterTopic = "home/controllers/temp";
    final SensorSwitch status = new SensorSwitch(true);
    final String brokerAddress, clientId;
    private MqttClient mqttClient;
    final int qos;
    private boolean currentStatus = false;

    public Heaters(String brokerAddress, String clientId, int qos) {
        if (qos > 2 || qos < 0) {
            throw new IllegalArgumentException();
        }
        this.brokerAddress = brokerAddress;
        this.clientId = clientId;
        this.qos = qos;
    }

    @Override
    public void run() {
        try {
            this.connect();
            this.subscribe();
        } catch (MqttException e) {
            e.printStackTrace();
            return;
        }
        while (this.status.isOn()) {
            try {
                this.status.waitForShutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            this.mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void connect() throws MqttException {
        this.mqttClient = new MqttClient(this.brokerAddress, this.clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setKeepAliveInterval(60);
        this.mqttClient.connect(options);
    }

    private void subscribe() throws MqttException {
        Heaters currentInstance = this;
        this.mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                cause.printStackTrace();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                boolean heatersOn = Boolean.parseBoolean(new String(message.getPayload()));
                if (heatersOn) {
                    if (!currentInstance.currentStatus) {
                        System.out.println("Heaters on");
                        currentInstance.currentStatus = true;
                    }
                } else {
                    if (currentInstance.currentStatus) {
                        System.out.println("Heaters off");
                        currentInstance.currentStatus = false;
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
        System.out.println("Heaters off");
        this.mqttClient.subscribe(this.heaterTopic);
    }

    public void shutdown() {
        this.status.shutdown();
    }
}

class SensorSwitch {
    private boolean status;

    public SensorSwitch(boolean status) {
        this.status = status;
    }

    public synchronized boolean isOn() {
        return this.status;
    }

    public synchronized void shutdown() {
        this.status = false;
        notifyAll();
    }

    public synchronized void waitForShutdown() throws InterruptedException {
        while (this.status) {
            wait();
        }
    }
}
