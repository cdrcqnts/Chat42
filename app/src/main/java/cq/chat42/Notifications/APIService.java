package cq.chat42.Notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                "Content-Type:application/json",
                    "Authorization:key=AAAA8wf8hvk:APA91bFCvzFDEVsOEENVmX0B6V4vybaF--gIaWceYWpl9zL0PxODAcMQqQnsi6Ktizm3ceMtLz-rZVkVoUjYwHCFyGuHJ9nhoDzs7UYzmV9XwiN29lSLOiyVZKY-ZS5fpzclTNth5p4L"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
