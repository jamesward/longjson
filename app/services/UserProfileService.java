package services;

import models.UserProfile;
import play.libs.F;
import services.ws.UserProfileWS;
import utils.MemCachierClient;

public class UserProfileService {

    public static F.Promise<UserProfile> get() {
        UserProfile maybeUserProfile = (UserProfile) MemCachierClient.getInstance().get("userprofile");

        if (maybeUserProfile == null) {
            return F.Promise.wrap(UserProfileWS.get()).map( userProfile -> {
                MemCachierClient.getInstance().set("userprofile", 60 * 60, userProfile);
                return userProfile;
            });
        }
        else {
            return F.Promise.pure(maybeUserProfile);
        }
    }

}
