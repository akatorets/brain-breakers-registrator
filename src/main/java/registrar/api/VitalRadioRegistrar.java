package registrar.api;

import com.vk.api.sdk.client.actors.UserActor;
import registrar.exception.TokenException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static registrar.Constants.APPLICATION_ID;
import static registrar.Constants.LOGIN;
import static registrar.Constants.PASSWORD;
import static registrar.Constants.SCOPE;
import static registrar.Constants.USER_ID;

public class VitalRadioRegistrar {
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Create the registration task.
     * @param registrationStartDate the registration start date
     */
    public void register(String registrationStartDate) {
        String token = getToken();

        UserActor userActor = new UserActor(USER_ID, token);
        RegistrationTask task = new RegistrationTask(userActor);

        Date date = convertDate(registrationStartDate);
        scheduler.schedule(task, date.getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    private String getToken() {
        try {
            return VkTokenGetter.getAccessToken(APPLICATION_ID, SCOPE, LOGIN, PASSWORD);
        } catch (IOException e) {
            throw new TokenException("Couldn't retrieve access token: " + e.getMessage(), e);
        }
    }

    /**
     * Convert the start registration date from string and return the date 20 seconds before.
     * @param registrationStartDate the start registration date
     * @return the date 20 seconds before the start of registration
     */
    private Date convertDate(String registrationStartDate) {
        Date date = null;
        try {
            date = format.parse(registrationStartDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Can not convert date from " + registrationStartDate);
        }
        // return the date 20 seconds before the start of registration
        return new Date(date.getTime() - 20000);
    }

}
