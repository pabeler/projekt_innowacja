import net.aksingh.owmjapis.api.APIException;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;
import net.aksingh.owmjapis.model.HourlyWeatherForecast;
import net.aksingh.owmjapis.model.param.WeatherData;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Weather extends JFrame {
    static OWM owm = new OWM("8169e266ba742ce4e86029d3417af558");
    static CurrentWeather cwd;
    static HourlyWeatherForecast hwd;
    private JPanel panel1;
    private JPanel mainPanel;
    private JLabel cityName;
    private JTextField cityNameInput;
    private JButton submitButton;

    public Weather() {
        super("Weather");
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setSize(500, 100);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    cwd = owm.currentWeatherByCityName(cityNameInput.getText());
                    hwd = owm.hourlyWeatherForecastByCityName(cityNameInput.getText());
                } catch (APIException e1) {
                    JOptionPane.showMessageDialog(submitButton, "City not found");
                    throw new RuntimeException(e1);
                }
                assert cwd.getMainData() != null;
                StringBuilder weatherInfo = new StringBuilder();
                weatherInfo.append("Tempearature for ").append(cwd.getCityName()).append(" is ")
                        .append(kelvinToCelsius(cwd.getMainData().getTemp())).append("°C").append("\n\n")
                        .append("Weather forecast: ").append("\n");
                assert hwd.getDataList() != null;
                for (int i = 0; i < hwd.getDataList().size(); i += 2) {
                    WeatherData data = hwd.getDataList().get(i);
                    assert data.getMainData() != null;
                    weatherInfo.append(data.getDateTime()).append(" ")
                            .append(kelvinToCelsius(data.getMainData().getTemp())).append("°C").append("\n");
                }
                JOptionPane.showMessageDialog(submitButton, weatherInfo);
            }
        });
    }

    Double kelvinToCelsius(Double kelvin) {
        return (double) Math.round(kelvin - 273.15);
    }
}
