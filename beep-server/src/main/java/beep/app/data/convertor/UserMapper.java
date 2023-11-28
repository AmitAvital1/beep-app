package beep.app.data.convertor;

import beep.app.data.dao.UserRepository;
import beep.app.data.entities.UserEntity;
import beep.engine.user.User;
import beep.engine.user.UserImpl;

public class UserMapper {
    public static UserEntity toJpaEntityNoInvitations(User engineUser){
        UserEntity userEntity = new UserEntity(engineUser.userID(), engineUser.getFirstName(), engineUser.getLastName(), engineUser.getAreaCode(), engineUser.getPhoneNumber());
        return userEntity;
    }
    public static User toEngineEntityNoInvitations(UserEntity userEntity){
        User engineUser = new UserImpl(userEntity.getUUID(),userEntity.getFirstName(),userEntity.getLastName(),userEntity.getPhoneAreaCode(),userEntity.getPhoneNumber());
        return engineUser;
    }
}
