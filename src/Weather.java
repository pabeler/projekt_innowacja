import net.aksingh.owmjapis.api.APIException;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;
import net.aksingh.owmjapis.model.HourlyWeatherForecast;
import net.aksingh.owmjapis.model.param.WeatherData;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

public class Weather extends JFrame {
    static OWM owm = new OWM("8169e266ba742ce4e86029d3417af558");
    static CurrentWeather cwd;
    static HourlyWeatherForecast hwd;
    private JPanel panel1;
    private JPanel mainPanel;
    private JTextField cityNameInput;
    private JButton submitButton;
    private JLabel cityNameLabel;
    private JLabel currentWeather;
    private JLabel weatherForecast;

    public Weather() {
        super("Weather");
        ImageIcon icon = new ImageIcon(".\\icons\\search.png");
        submitButton.setIcon(icon);
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setSize(500, 500);
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
                try {
                    assert cwd.getWeatherList() != null;
                    currentWeather.setIcon(new ImageIcon(ImageIO.read(new URL(cwd.getWeatherList().get(0).getIconLink()))));
                    currentWeather.setText("Tempearature for " + cwd.getCityName() + " is "
                            + kelvinToCelsius(cwd.getMainData().getTemp()) + "°C");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                StringBuilder forecastInfo = new StringBuilder();
                forecastInfo.append("<html>").append("Weather forecast: ").append("<br/>");
                for (int i = 0; i < hwd.getDataList().size(); i += 2) {
                    WeatherData data = hwd.getDataList().get(i);
                    assert data.getMainData() != null;
                    forecastInfo.append(data.getDateTime()).append(" ")
                            .append(kelvinToCelsius(data.getMainData().getTemp())).append("°C").append("<br/>");
                }
                forecastInfo.append("</html>");
                JOptionPane.showMessageDialog(submitButton, forecastInfo);
                weatherForecast.setText(forecastInfo.toString());
//                System.out.println(cwd.getWeatherList().get(0).getDescription());
//                System.out.println(cwd.getWeatherList().get(0).getIconLink());
//                System.out.println(cwd.getWeatherList().get(0).getIconCode());
//                System.out.println(cwd.getWeatherList().get(0).getConditionId());
//                System.out.println(cwd.getWeatherList().get(0).getMainInfo());
//                System.out.println(cwd.getWeatherList().get(0).getMoreInfo());
            }
        });
    }

    Double kelvinToCelsius(Double kelvin) {
        return (double) Math.round(kelvin - 273.15);
    }
}
