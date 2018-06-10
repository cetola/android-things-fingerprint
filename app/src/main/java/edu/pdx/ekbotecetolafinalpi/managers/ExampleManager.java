package edu.pdx.ekbotecetolafinalpi.managers;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Random;

import edu.pdx.ekbotecetolafinalpi.account.User;
import edu.pdx.ekbotecetolafinalpi.dao.UserDao;
import edu.pdx.ekbotecetolafinalpi.dao.UserDaoImpl;

public class ExampleManager {

    private static final String TAG = "ExampleManager";
    private UserDao userDao;
    private Random rand;
    private User mUser;

    public ExampleManager(FirestoreManager dbManager) {
        rand = new Random();
        userDao = new UserDaoImpl(dbManager);
        doUserSaveExample();
    }
    
    private int getPostfix() {
        return rand.nextInt(500);
    }

    private void doUserSaveExample() {
        final User user = new User();
        user.setFirstName("exFirst" + getPostfix());
        user.setLastName("exLast" + getPostfix());
        user.setUsername("exName" + getPostfix());
        userDao.saveUser(user, new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                user.setId(documentReference.getId());
                mUser = user;
                doPrintUser();
                doUserGetExample();
            }
        });
    }

    private void doUserGetExample() {
        final String userId = mUser.getId();
        mUser = null;

        userDao.getUserById(userId, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    User u = documentSnapshot.toObject(User.class);
                    u.setId(documentSnapshot.getId());
                    mUser = u;
                    doPrintUser();
                }
            }
        });
    }
    
    private void doPrintUser() {
        Log.d(TAG, "doPrintUsers: " + mUser.getUsername());
    }
}
