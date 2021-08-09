package sample;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Saved_Chart_Controller implements Initializable {

    private StringBuilder builder = new StringBuilder();
    private Controller parent_controller;
    private File fileObject;
    private final XYChart.Series<String, Number> Position_Switch = new XYChart.Series<String, Number>();
    private final XYChart.Series<String, Number> Three_State_Switch = new XYChart.Series<String, Number>();
    private final XYChart.Series<String, Number> Brake_Potentiometer = new XYChart.Series<String, Number>();
    private final XYChart.Series<String, Number> Alarm_Switch = new XYChart.Series<String, Number>();
    private final XYChart.Series<String, Number> Position_Leds = new XYChart.Series<String, Number>();
    private final XYChart.Series<String, Number> Left_Turn_Led = new XYChart.Series<String, Number>();
    private final XYChart.Series<String, Number> Right_Turn_Led = new XYChart.Series<String, Number>();
    private final XYChart.Series<String, Number> Brake_Led = new XYChart.Series<String, Number>();
    private final List<XYChart.Data<String, Number>> position_switch_list = new ArrayList<>();
    private final List<XYChart.Data<String, Number>> three_state_switch_list = new ArrayList<XYChart.Data<String, Number>>();
    private final List<XYChart.Data<String, Number>> alarm_switch_list = new ArrayList<XYChart.Data<String, Number>>();
    private final List<XYChart.Data<String, Number>> brake_potentiometer_list = new ArrayList<XYChart.Data<String, Number>>();
    private final List<XYChart.Data<String, Number>> position_led_list = new ArrayList<XYChart.Data<String, Number>>();
    private final List<XYChart.Data<String, Number>> left_turn_led_list = new ArrayList<XYChart.Data<String, Number>>();
    private final List<XYChart.Data<String, Number>> right_turn_led_list = new ArrayList<XYChart.Data<String, Number>>();
    private final List<XYChart.Data<String, Number>> brake_led_list = new ArrayList<XYChart.Data<String, Number>>();

    final int WINDOW_SIZE = 300;
    int index = 0;

    @FXML
    private AnchorPane Line_chart;

    @FXML
    private LineChart<String, Number> lineChart;

    @FXML
    private ComboBox<String> Chart_List;

    @FXML
    private Button Start_button;

    @FXML
    private Button Stop_Button;

    @FXML
    private Label error;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
    //    lineChart.getData().addAll(Position_Switch);
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);

     //   Chart_List.getSelectionModel().select("Position_Switch");
    }


    @FXML
    void changeChart(ActionEvent event) {
        XYChart.Series<String, Number> series;
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
        System.out.println(series.getName());

    }

    @FXML
    void start_session(ActionEvent event) {
        String[] str = readFile().trim().split("\n");
        Integer[][] bytes = new Integer[str.length][10];
        for (int i = 0;i < str.length;i++) {
            String[] elems = str[i].split(",");
            elems[0] = elems[0].substring(1);
            elems[elems.length - 1] = elems[elems.length - 1].substring(0, elems[elems.length - 1].length() - 1);
            for (int j = 0; j < elems.length; j++) {
                    bytes[i][j] = Integer.parseInt(elems[j].trim());
            }
        }
            Coordinate[][] coordinates = new Coordinate[8][str.length];
            for (int i = 0; i < str.length; i++) {
                coordinates[0][i] = new Coordinate(index++, bytes[i][1]);
                coordinates[1][i] = new Coordinate(index,(bytes[i][3] << 8) | (bytes[i][2] & 0xff));
                coordinates[2][i] = new Coordinate(index, bytes[i][4]);
                coordinates[3][i] = new Coordinate(index, bytes[i][5]);
                coordinates[4][i] = new Coordinate(index, ((bytes[i][6] == 1) || (bytes[i][6] == 3)) ? 1 : 0);
                coordinates[5][i] = new Coordinate(index, ((bytes[i][6] == 2) || (bytes[i][6] == 3)) ? 1 : 0);
                coordinates[6][i] = new Coordinate(index, bytes[i][7] & 0xff);
                coordinates[7][i] = new Coordinate(index, bytes[i][8] & 0xff);
            }
            Geometry[] geom = new Geometry[8];
            Geometry[] simplified = new Geometry[8];
            List<ObservableList<XYChart.Data<String, Number>>> list = new ArrayList<>();
            List<XYChart.Data<String, Number>>[] update = new List[8];
            GeometryFactory gf = new GeometryFactory();
            for (int i = 0;i < 8;i++){
                CoordinateArraySequence coord = new CoordinateArraySequence(coordinates[i]);
                geom[i] = new LineString(new CoordinateArraySequence(coordinates[i]), gf);
                simplified[i] = DouglasPeuckerSimplifier.simplify(geom[i], 0.00001);
                update[i] = new ArrayList<>();
                for (Coordinate each : simplified[i].getCoordinates()) {
                    update[i].add(new XYChart.Data<>(String.valueOf(each.x), each.y));
                }
                list.add(FXCollections.observableArrayList(update[i]));
            }
        lineChart.setPrefWidth(str.length);
        Line_chart.setPrefWidth(str.length);

            updateChart(list);

//            position_switch_list.add(new XYChart.Data<>(String.valueOf(index++), bytes[1]));
//            three_state_switch_list.add(new XYChart.Data<>(String.valueOf(index),bytes[4]));
//            alarm_switch_list.add(new XYChart.Data<>(String.valueOf(index), bytes[5]));
//            brake_potentiometer_list.add(new XYChart.Data<>(String.valueOf(index),(bytes[3] << 8) | (bytes[2] & 0xff)));
//            position_led_list.add(new XYChart.Data<>(String.valueOf(index),bytes[7] & 0xff));
//            left_turn_led_list.add(new XYChart.Data<>(String.valueOf(index),((bytes[6] == 1) || (bytes[6] == 3)) ? 1 : 0));
//            right_turn_led_list.add(new XYChart.Data<>(String.valueOf(index),((bytes[6] == 2) || (bytes[6] == 3)) ? 1 : 0));
//            brake_led_list.add(new XYChart.Data<>(String.valueOf(index),bytes[8] & 0xff));


        System.out.println(12);
    }

    public  void updateChart( List<ObservableList<XYChart.Data<String, Number>>> list) {
        Platform.runLater(() -> {
            Position_Switch.setData(list.get(0));
            Three_State_Switch.setData(list.get(2));
            Brake_Potentiometer.setData(list.get(1));
            Alarm_Switch.setData(list.get(3));
            Position_Leds.setData(list.get(6));
            Left_Turn_Led.setData(list.get(4));
            Right_Turn_Led.setData(list.get(5));
            Brake_Led.setData(list.get(7));

//            if (Position_Switch.getData().size() > WINDOW_SIZE) {
//                Position_Switch.getData().remove(0);
//                Three_State_Switch.getData().remove(0);
//                Brake_Potentiometer.getData().remove(0);
//                Alarm_Switch.getData().remove(0);
//                Position_Leds.getData().remove(0);
//                Left_Turn_Led.getData().remove(0);
//                Right_Turn_Led.getData().remove(0);
//                Brake_Led.getData().remove(0);
//            }
        });


    }

    @FXML
    void stop_session(ActionEvent event) {

    }

    public void setFileObject(File fileObject)  {
        this.fileObject = fileObject;
    }

    public void setParent_controller(Controller parent_controller) {
        this.parent_controller = parent_controller;
    }

    private  String readFile() {
        String line;
        try (BufferedReader in = new BufferedReader(new FileReader(fileObject))) {
            while ((line = in.readLine()) != null) {
                builder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}
