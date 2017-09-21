package com.mainstreetcode.teammates.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammates.notifications.JoinRequestNotifier;
import com.mainstreetcode.teammates.notifications.Notifiable;
import com.mainstreetcode.teammates.notifications.Notifier;
import com.mainstreetcode.teammates.persistence.entity.JoinRequestEntity;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.lang.reflect.Type;

/**
 * Join request for a {@link Team}
 */

public class JoinRequest extends JoinRequestEntity
        implements
        Model<JoinRequest>,
        Notifiable<JoinRequest> {

    public static JoinRequest join(String roleName, String teamId, User user) {
        return new JoinRequest(false, true, "", roleName, teamId, user);
    }

    public static JoinRequest invite(String roleName, String teamId, String firstName, String lastName, String email) {
        User user = new User("", firstName, lastName, email, "");
        return new JoinRequest(true, false, "", roleName, teamId, user);
    }

    JoinRequest(boolean teamApproved, boolean userApproved, String id, String roleName, String teamId, User user) {
        super(teamApproved, userApproved, id, roleName, teamId, user);
    }

    protected JoinRequest(Parcel in) {
        super(in);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public String getImageUrl() {
        return user == null ? null : user.getImageUrl();
    }

    @Override
    public void update(JoinRequest updated) {
        this.teamApproved = updated.teamApproved;
        this.userApproved = updated.userApproved;
        this.id = updated.id;
        this.roleName = updated.roleName;
        this.teamId = updated.teamId;
        user.update(updated.user);
    }

    @Override
    public Notifier<JoinRequest> getNotifier() {
        return JoinRequestNotifier.getInstance();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Parcelable.Creator<JoinRequest> CREATOR = new Parcelable.Creator<JoinRequest>() {
        @Override
        public JoinRequest createFromParcel(Parcel in) {
            return new JoinRequest(in);
        }

        @Override
        public JoinRequest[] newArray(int size) {
            return new JoinRequest[size];
        }
    };

    public static class GsonAdapter
            implements
            JsonSerializer<JoinRequest>,
            JsonDeserializer<JoinRequest> {

        private static final String ID_KEY = "_id";
        private static final String NAME_KEY = "roleName";
        private static final String USER_KEY = "user";
        private static final String USER_FIRST_NAME_KEY = "firstName";
        private static final String USER_LAST_NAME_KEY = "lastName";
        private static final String USER_PRIMARY_EMAIL_KEY = "primaryEmail";
        private static final String TEAM_KEY = "team";
        private static final String TEAM_APPROVAL_KEY = "teamApproved";
        private static final String USER_APPROVAL_KEY = "userApproved";

        @Override
        public JsonElement serialize(JoinRequest src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();

            result.addProperty(NAME_KEY, src.roleName);
            result.addProperty(TEAM_KEY, src.teamId);
            result.addProperty(TEAM_APPROVAL_KEY, src.teamApproved);
            result.addProperty(USER_APPROVAL_KEY, src.userApproved);

            User user = src.user;

            if (src.teamApproved) {
                result.addProperty(USER_FIRST_NAME_KEY, user.getFirstName());
                result.addProperty(USER_LAST_NAME_KEY, user.getLastName());
                result.addProperty(USER_PRIMARY_EMAIL_KEY, user.getPrimaryEmail());
            }
            else {
                result.addProperty(USER_KEY, src.getUser().getId());
            }

            return result;
        }

        @Override
        public JoinRequest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject requestJson = json.getAsJsonObject();

            boolean teamApproved = ModelUtils.asBoolean(TEAM_APPROVAL_KEY, requestJson);
            boolean userApproved = ModelUtils.asBoolean(USER_APPROVAL_KEY, requestJson);

            String id = ModelUtils.asString(ID_KEY, requestJson);
            String roleName = ModelUtils.asString(NAME_KEY, requestJson);
            String teamId = ModelUtils.asString(TEAM_KEY, requestJson);
            User user = context.deserialize(requestJson.get(USER_KEY), User.class);

            if (user == null) user = User.empty();

            return new JoinRequest(teamApproved, userApproved, id, roleName, teamId, user);
        }
    }
}
