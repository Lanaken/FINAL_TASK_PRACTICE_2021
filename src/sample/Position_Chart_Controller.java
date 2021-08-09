package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import jssc.*;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class Position_Chart_Controller implements Initializable {

    @FXML
    private LineChart<String, Integer> lineChart;

    String[] portNames = SerialPortList.getPortNames();

    @FXML
    private Label error;

    @FXML
    private Button Start_button;

    @FXML
    private Button Stop_Button;

    @FXML
    private ComboBox<String> Chart_List;

    private String current_chart;


    @FXML
    private ComboBox<String> COM_PORTS;

    private Controller parent_controller;
    private BufferedOutputStream osr;
    private PrintWriter out;
    private File fileObject;
    private static SerialPort serialPort;
    private final XYChart.Series<String, Integer> Position_Switch = new XYChart.Series<>();
    private final XYChart.Series<String, Integer> Three_State_Switch = new XYChart.Series<>();
    private final XYChart.Series<String, Integer> Brake_Potentiometer = new XYChart.Series<>();
    private final XYChart.Series<String, Integer> Alarm_Switch = new XYChart.Series<>();
    private final XYChart.Series<String, Integer> Position_Leds = new XYChart.Series<>();
    private final XYChart.Series<String, Integer> Left_Turn_Led = new XYChart.Series<>();
    private final XYChart.Series<String, Integer> Right_Turn_Led = new XYChart.Series<>();
    private final XYChart.Series<String, Integer> Brake_Led = new XYChart.Series<>();
    final int WINDOW_SIZE = 300;
    int index = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        COM_PORTS.getItems().addAll(portNames);
        Chart_List.getItems().addAll(
                "Position_Switch",
                "Three_State_Switch",
                "Brake_Potentiometer",
                "Alarm_Switch",
                "Position_Leds",
                "Left_Turn_Led",
                "Right_Turn_Led",
                "Brake_Led"
        );
        lineChart.getData().addAll(Position_Switch);
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);
        Chart_List.getSelectionModel().select("Position_Switch");
    }

    private void open_serial_port(){
        if (serialPort != null && serialPort.isOpened())
            return;
        serialPort = new SerialPort(COM_PORTS.getSelectionModel().getSelectedItem()); /*Передаем в конструктор суперкласса имя порта с которым будем работать*/
        try {
            serialPort.openPort(); /*Метод открытия порта*/
            serialPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE); /*Задаем основные параметры протокола UART*/
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                    SerialPort.FLOWCONTROL_RTSCTS_OUT);
            serialPort.setEventsMask(SerialPort.MASK_RXCHAR); /*Устанавливаем маску или список события на которые будет происходить реакция. В данном случае это приход данных в буффер порта*/
            serialPort.addEventListener(new EventListener()); /*Передаем экземпляр класса EventListener порту, где будет обрабатываться события. Ниже описан класс*/
        } catch (SerialPortException ex) {
            error = new Label(ex.getMessage());
        }
    }

    public void setParent_controller(Controller parent_controller) {
        this.parent_controller = parent_controller;
    }

    @FXML
    public void start_session(ActionEvent actionEvent){

        open_serial_port();
    }

    @FXML
    public void stop_session(ActionEvent actionEvent) throws SerialPortException {
        if (serialPort != null && !serialPort.isOpened())
            return;
        serialPort.closePort();
    }

    private void setParent_file_message(String fileMessage){
        parent_controller.setFileMessage(fileMessage);
    }


    @FXML
    public void changeChart(ActionEvent actionEvent){
        XYChart.Series<String, Integer> series;
        switch (Chart_List.getSelectionModel().getSelectedItem()){
            case ("Position_Switch"):{
                series = Position_Switch;
                break;
            }
            case ("Three_State_Switch"):{
                series = Three_State_Switch;
                break;
            }
            case ("Brake_Potentiometer"):{
                series = Brake_Potentiometer;
                break;
            }
            case ("Alarm_Switch"):{
                series = Alarm_Switch;
                break;
            }
            case ("Position_Leds"):{
                series = Position_Leds;
                break;
            }
            case ("Left_Turn_Led"):{
                series = Left_Turn_Led;
                break;
            }
            case ("Right_Turn_Led"):{
                series = Right_Turn_Led;
                break;
            }
            case ("Brake_Led"):{
                series = Brake_Led;
                break;
            }


            default:
                throw new IllegalStateException("Unexpected value: " + Chart_List.getSelectionModel().getSelectedItem());
        }
        series.setName(Chart_List.getSelectionModel().getSelectedItem());
        lineChart.getData().clear();
        lineChart.getData().add(series);
        //lineChart.getData() = series;
        System.out.println(series.getName());
    }
    public void clickClose(ActionEvent actionEvent) throws SerialPortException {
        Node n = (Node)actionEvent.getSource();
        Stage stage = (Stage)n.getScene().getWindow();
        serialPort.closePort();
        stage.close();
    }

    public void setFileObject(File fileObject)  {
        this.fileObject = fileObject;
    }

    public  void updateChart(byte[] point) {

        Platform.runLater(() -> {
            Position_Switch.getData().add(new XYChart.Data<>(String.valueOf(index++),(int) point[1]));
            Three_State_Switch.getData().add(new XYChart.Data<>(String.valueOf(index),(int) point[4]));
            Brake_Potentiometer.getData().add(new XYChart.Data<>(String.valueOf(index),(point[3] << 8) | (point[2] & 0xff)));
            Alarm_Switch.getData().add(new XYChart.Data<>(String.valueOf(index),(int) point[5]));
            Position_Leds.getData().add(new XYChart.Data<>(String.valueOf(index),point[7] & 0xff));
            Left_Turn_Led.getData().add(new XYChart.Data<>(String.valueOf(index),((point[6] == 1) || (point[6] == 3)) ? 1 : 0));
            Right_Turn_Led.getData().add(new XYChart.Data<>(String.valueOf(index),((point[6] == 2) || (point[6] == 3)) ? 1 : 0));
            Brake_Led.getData().add(new XYChart.Data<>(String.valueOf(index),point[8] & 0xff));
            if (Position_Switch.getData().size() > WINDOW_SIZE) {
                Position_Switch.getData().remove(0);
                Three_State_Switch.getData().remove(0);
                Brake_Potentiometer.getData().remove(0);
                Alarm_Switch.getData().remove(0);
                Position_Leds.getData().remove(0);
                Left_Turn_Led.getData().remove(0);
                Right_Turn_Led.getData().remove(0);
                Brake_Led.getData().remove(0);
            }


        });



    }


    class EventListener implements SerialPortEventListener {
        public void serialEvent(SerialPortEvent event) {
            PrintWriter out = null;
            if (event.isRXCHAR() && event.getEventValue() > 9) { /*Если происходит событие установленной маски и количество байтов в буфере более 0*/
                byte[] position = null;
                try {
                    position = serialPort.readBytes();
                } catch (SerialPortException ex) {
                    System.out.println(ex);
                }
                /*Сохраняем данные в текстовый файл, дозапись*/
                try {
                    if(position != null)
                        if (position.length == 10) {
                            updateChart(position);
                            FileOutputStream fos = new FileOutputStream(fileObject, true);

                            //osr = new BufferedOutputStream(fos);/*Запись потоков необработанных байтов в текстовый файл. В данном случае создает поток вывода файла, чтобы записать в файл с указанным именем */

                            OutputStreamWriter osr = new OutputStreamWriter(fos,"Cp866");  /*Создаем объект для вывода данных в указанный поток fos, с учетом заданной кодировки символов Cp866*/
                            out = new PrintWriter(osr);/*Символьный класс включающий методы print () и println (). Непосредственно производим операции записи в файл*/
                            out.println(Arrays.toString(position));
                            String[] ary = Arrays.toString(position).split(",");
                            System.out.println(Arrays.toString(position));/*Запись данных с новой строки*/
                        }
                } catch (IOException ex) {
                } finally {
                    if (out != null) {
                        try {
                            out.close();     /*Закрытие файла*/
                        } catch (Exception ex) {
                        }
                    }
                }
            }
        }

    }
}
