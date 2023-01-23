import net.aksingh.owmjapis.api.APIException;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;
import net.aksingh.owmjapis.model.HourlyWeatherForecast;
import net.aksingh.owmjapis.model.param.Coord;
import net.aksingh.owmjapis.model.param.WeatherData;
import net.iakovlev.timeshape.TimeZoneEngine;
import org.joda.time.DateTime;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.TimeZone;

public class Weather extends JFrame {
    static OWM owm;
    static CurrentWeather cwd;
    static HourlyWeatherForecast hwd;
    private JPanel panel1;
    private JPanel mainPanel;
    private JTextField cityNameInput;
    private JButton submitButton;
    private JLabel cityNameLabel;
    private JScrollPane weatherForecastPane;
    private JLabel weatherForecastLabel;
    private JComboBox modeSelectionComboBox;
    private JButton settingsButton;
    private TimeZoneEngine engine = TimeZoneEngine.initialize();

    public Weather() {
        super("Weather");
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 110);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        submitButton.setIcon(new ImageIcon(".\\icons\\search.png"));
        settingsButton.setIcon(new ImageIcon(".\\icons\\settings.png"));
        cityNameInput.setForeground(Color.GRAY);
        weatherForecastPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        weatherForecastPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        while (true) {
            try {
                owm = new OWM((String) JOptionPane.showInputDialog(null,
                        "Enter your openweather api key", "API Key input",
                        JOptionPane.PLAIN_MESSAGE, null, null, null));
            } catch (Exception ignored) {
                System.exit(0);
            }
            try {
                owm.currentWeatherByCityName("London");
                break;
            } catch (APIException e) {
                JOptionPane.showMessageDialog(null, "Invalid API key", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        try {
            File myObj = new File("predefinedCityName.txt");
            Scanner myReader = new Scanner(myObj);
            String data = myReader.nextLine();
            cwd = owm.currentWeatherByCityName(data);
            assert cwd.getMainData() != null;
            JOptionPane.showMessageDialog(null, "The temperature in " + cwd.getCityName()
                            + " is: " + kelvinToCelsius(cwd.getMainData().getTemp()) + "°C",
                    "Temperature in predefined localization", JOptionPane.INFORMATION_MESSAGE);
            myReader.close();
        } catch (FileNotFoundException ignored) {} catch (APIException e) {
            throw new RuntimeException(e);
        }
        setVisible(true);
        cityNameInput.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (cityNameInput.getText().equals("Insert city name here")) {
                    cityNameInput.setText("");
                    cityNameInput.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (cityNameInput.getText().isEmpty()) {
                    cityNameInput.setForeground(Color.GRAY);
                    cityNameInput.setText("Insert city name here");
                }
            }
        });
        modeSelectionComboBox.setSelectedIndex(-1);
        modeSelectionComboBox.setEnabled(false);
        settingsButton.addActionListener(e -> {
            String inputDialogValue = (String) JOptionPane.showInputDialog(null,
                    "Enter the name of the city to be displayed on start-up", "Settings",
                    JOptionPane.PLAIN_MESSAGE, null, null, null);
            if (inputDialogValue != null) {
                try {
                    owm.currentWeatherByCityName(inputDialogValue);
                    File f = new File("predefinedCityName.txt");
                    FileWriter fw = new FileWriter(f);
                    if (!f.createNewFile()) {
                        fw.flush();
                    }
                    fw.write(inputDialogValue);
                    fw.close();
                    JOptionPane.showMessageDialog(null, "The predefined city was changed");
                } catch (APIException | IOException ex) {
                    JOptionPane.showMessageDialog(submitButton, "City not found");
                }
            }
        });
        submitButton.addActionListener(e -> {
            try {
                cwd = owm.currentWeatherByCityName(cityNameInput.getText());
                hwd = owm.hourlyWeatherForecastByCityName(cityNameInput.getText());
            } catch (APIException e1) {
                JOptionPane.showMessageDialog(submitButton, "City not found");
            }
            setSize(600, 140);
            JOptionPane.showMessageDialog(submitButton, "City found" + "\n"
                    + "Select what you want to display using the drop-down list");
            modeSelectionComboBox.setEnabled(true);
            modeSelectionComboBox.addActionListener(e12 -> {
                String selected = (String) modeSelectionComboBox.getSelectedItem();
                assert selected != null;
                switch (selected) {
                    case "Current weather" -> {
                        setSize(600, 250);
                        try {
                            assert cwd.getWeatherList() != null;
                            weatherForecastLabel.setIcon(new ImageIcon(ImageIO.read(new URL(cwd.getWeatherList()
                                    .get(0).getIconLink()))));
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        assert cwd.getMainData() != null;
                        assert cwd.getWindData() != null;
                        String sb = "<html>" +
                                "Selected city: " + cwd.getCityName() + "<br>" +
                                "Temperature: " + kelvinToCelsius(cwd.getMainData().getTemp()) + " °C" +
                                "<br>" +
                                "Pressure: " + cwd.getMainData().getPressure() + " hPa" + "<br>" +
                                "Humidity: " + cwd.getMainData().getHumidity() + " %" + "<br>" +
                                "Wind speed: " + (int) (mpsToKmph(cwd.getWindData().getSpeed())) + " km/h" +
                                "<br>" +
                                "Wind direction: " + windDegreeToDirection(cwd.getWindData().getDegree()) +
                                "</html>";
                        weatherForecastLabel.setText(sb);
                    }
                    case "Weather forecast" -> {
                        setSize(600, 475);
                        StringBuilder forecastInfo = new StringBuilder();
                        forecastInfo.append("<html>");
                        assert hwd.getDataList() != null;
                        assert hwd.getCityData() != null;
                        forecastInfo.append("Selected city: ").append(hwd.getCityData().getName()).append("<br>");
                        for (WeatherData data : hwd.getDataList()) {
                            DateTime dateTime = new DateTime(data.getDateTime());
                            assert hwd.getCityData().getCoordData() != null;
                            dateTime = dateTime.plusHours(timeZoneOffset(hwd.getCityData().getCoordData()));
                            forecastInfo.append("[")
                                    .append(dateTime.getDayOfMonth()).append(".")
                                    .append(dateTime.getMonthOfYear()).append(".")
                                    .append(dateTime.getYear()).append(" ")
                                    .append(dateTime.getHourOfDay()).append(":00] ")
                                    .append("<br>");
                            assert data.getMainData() != null;
                            forecastInfo.append("Temperature: ").append(kelvinToCelsius(data.getMainData().getTemp()))
                                    .append(" °C").append("<br>");
                            forecastInfo.append("Pressure: ").append(data.getMainData().getPressure()).append(" hPa")
                                    .append("<br>");
                            forecastInfo.append("Humidity: ").append(data.getMainData().getHumidity()).append(" %")
                                    .append("<br>");
                            assert data.getWindData() != null;
                            forecastInfo.append("Wind speed: ").append((int) (mpsToKmph(data.getWindData().getSpeed())))
                                    .append(" km/h").append("<br>");
                            forecastInfo.append("Wind direction: ").append(windDegreeToDirection(data.getWindData()
                                    .getDegree())).append("<br>");
                            forecastInfo.append("<br>");
                        }
                        forecastInfo.append("</html>");
                        weatherForecastLabel.setIcon(null);
                        weatherForecastLabel.setText(forecastInfo.toString());
                    }
                    case "Detailed weather view" -> {
                        setSize(600, 270);
                        StringBuilder detailedWeatherInfo = new StringBuilder();
                        detailedWeatherInfo.append("<html>");
                        detailedWeatherInfo.append("Selected city: ").append(cwd.getCityName()).append("<br>");
                        DateTime currentDateTime = new DateTime(cwd.getDateTime());
                        assert cwd.getCoordData() != null;
                        currentDateTime = currentDateTime.plusHours(timeZoneOffset(cwd.getCoordData()));
                        assert cwd.getMainData() != null;
                        detailedWeatherInfo.append("[")
                                .append(currentDateTime.getDayOfMonth()).append(".")
                                .append(currentDateTime.getMonthOfYear()).append(".")
                                .append(currentDateTime.getYear()).append(" ")
                                .append(currentDateTime.getHourOfDay()).append(":00] ")
                                .append("<br>")
                                .append("Max temperature: ")
                                .append(kelvinToCelsius(cwd.getMainData().getTempMax())).append("°C")
                                .append("<br/>")
                                .append("Min temperature: ")
                                .append(kelvinToCelsius(cwd.getMainData().getTempMin())).append("°C")
                                .append("<br/>")
                                .append("Rainfall amount: ");
                        if (cwd.getRainData() != null) {
                            detailedWeatherInfo.append(cwd.getRainData().getPrecipVol3h()).append("mm");
                        } else {
                            detailedWeatherInfo.append("0mm");
                        }
                        detailedWeatherInfo.append("<br/>");
                        detailedWeatherInfo.append("Wind speed: ");
                        assert cwd.getWindData() != null;
                        detailedWeatherInfo.append((int) (mpsToKmph(cwd.getWindData().getSpeed()))).append(" km/h");
                        detailedWeatherInfo.append("<br/>");
                        detailedWeatherInfo.append("Wind direction: ");
                        Double windDeg = cwd.getWindData().getDegree();
                        detailedWeatherInfo.append(windDegreeToDirection(windDeg));
                        detailedWeatherInfo.append("<br/>");
                        assert hwd.getDataList() != null;
                        DateTime forecastDateTime;
                        for (WeatherData data : hwd.getDataList()) {
                            forecastDateTime = new DateTime(data.getDateTime());
                            assert Objects.requireNonNull(hwd.getCityData()).getCoordData() != null;
                            forecastDateTime = forecastDateTime.plusHours(timeZoneOffset(hwd.getCityData().getCoordData()));
                            if (!forecastDateTime.dayOfMonth().equals(currentDateTime.dayOfMonth())) {
                                break;
                            }
                            assert data.getTempData() != null;
                            assert data.getMainData() != null;
                            detailedWeatherInfo.append("<br/>")
                                    .append("[")
                                    .append(forecastDateTime.getDayOfMonth()).append(".")
                                    .append(forecastDateTime.getMonthOfYear()).append(".")
                                    .append(forecastDateTime.getYear()).append(" ")
                                    .append(forecastDateTime.getHourOfDay()).append(":00] ")
                                    .append("<br>")
                                    .append("Max temperature: ")
                                    .append(kelvinToCelsius(data.getMainData().getTempMax())).append("°C")
                                    .append("<br/>")
                                    .append("Min temperature: ")
                                    .append(kelvinToCelsius(data.getMainData().getTempMin())).append("°C")
                                    .append("<br/>")
                                    .append("Rainfall amount: ");
                            if (cwd.getRainData() != null) {
                                detailedWeatherInfo.append(cwd.getRainData().getPrecipVol3h()).append("mm");
                            } else {
                                detailedWeatherInfo.append("0mm");
                            }
                            detailedWeatherInfo.append("<br/>");
                            assert data.getWindData() != null;
                            detailedWeatherInfo.append("Wind speed: ")
                                    .append((int) (mpsToKmph(data.getWindData().getSpeed()))).append(" km/h")
                                    .append("<br/>")
                                    .append("Wind direction: ");
                            windDeg = data.getWindData().getDegree();
                            detailedWeatherInfo.append(windDegreeToDirection(windDeg)).append("<br/>");
                        }
                        detailedWeatherInfo.append("</html>");
                        weatherForecastLabel.setIcon(null);
                        weatherForecastLabel.setText(detailedWeatherInfo.toString());
                    }
                }
            });
        });
    }

    Double kelvinToCelsius(Double kelvin) {
        return (double) Math.round(kelvin - 273.15);
    }

    double mpsToKmph(Double mps) {
        return (double) Math.round(mps * 3.6);
    }

    int timeZoneOffset(Coord coord) {
        Optional<ZoneId> zoneId = engine.query(coord.getLatitude(), coord.getLongitude());
        return zoneId.get().getRules().getOffset(LocalDateTime.now(Clock.systemUTC())).getTotalSeconds() / 3600
                - defaultTimeZoneOffset();
    }

    int defaultTimeZoneOffset() {
        return TimeZone.getDefault().getDSTSavings() / 3600000;
    }

    String windDegreeToDirection(Double windDeg) {
        if (windDeg >= 0 && windDeg < 22.5) {
            return "N";
        } else if (windDeg >= 22.5 && windDeg < 67.5) {
            return "NE";
        } else if (windDeg >= 67.5 && windDeg < 112.5) {
            return "E";
        } else if (windDeg >= 112.5 && windDeg < 157.5) {
            return "SE";
        } else if (windDeg >= 157.5 && windDeg < 202.5) {
            return "S";
        } else if (windDeg >= 202.5 && windDeg < 247.5) {
            return "SW";
        } else if (windDeg >= 247.5 && windDeg < 292.5) {
            return "W";
        } else if (windDeg >= 292.5 && windDeg < 337.5) {
            return "NW";
        } else if (windDeg >= 337.5 && windDeg < 360) {
            return "N";
        } else {
            return "N/A";
        }
    }
}
