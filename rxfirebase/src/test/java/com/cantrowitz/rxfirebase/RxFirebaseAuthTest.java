package com.cantrowitz.rxfirebase;

import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import java.util.List;

import io.reactivex.observers.TestObserver;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RxFirebaseAuth}
 */
public class RxFirebaseAuthTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    FirebaseAuth firebaseAuth;

    @InjectMocks
    RxFirebaseAuth target;

    private FirebaseAuth.AuthStateListener authStateListener;

    @Before
    public void setUp() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                authStateListener = invocation.getArgument(0);

                return null;
            }
        }).when(firebaseAuth).addAuthStateListener(any(FirebaseAuth.AuthStateListener.class));
    }

    @Test
    public void testWhenDisposed_unregistersAuthStateListener() throws Exception {
        target.observeAuthChange().test(true);
        verify(firebaseAuth).removeAuthStateListener(authStateListener);
    }

    @Test
    public void testObserveAuthChange() throws Exception {

        FirebaseAuth test1 = createFirebaseAuth(null);
        FirebaseAuth test2 = createFirebaseAuth(null);

        final TestObserver<FirebaseAuth> observer = target.observeAuthChange().test();
        authStateListener.onAuthStateChanged(test1);
        authStateListener.onAuthStateChanged(test2);

        observer.assertValues(test1, test2);
    }

    @Test
    public void testObserveUserChange() throws Exception {

        FirebaseUser firebaseUser1 = mock(FirebaseUser.class);
        FirebaseUser firebaseUser2 = mock(FirebaseUser.class);

        FirebaseAuth firebaseAuth1 = createFirebaseAuth(firebaseUser1);
        FirebaseAuth firebaseAuth2 = createFirebaseAuth(firebaseUser2);
        FirebaseAuth firebaseAuth3 = createFirebaseAuth(null);

        final TestObserver<Maybe<FirebaseUser>> observer = target.observeUserChange().test();
        authStateListener.onAuthStateChanged(firebaseAuth1);
        authStateListener.onAuthStateChanged(firebaseAuth1);
        authStateListener.onAuthStateChanged(firebaseAuth2);
        authStateListener.onAuthStateChanged(firebaseAuth3);

        observer.assertValueCount(3);
        final List<Maybe<FirebaseUser>> values = observer.values();
        assertEquals(firebaseUser1, values.get(0).get());
        assertEquals(firebaseUser2, values.get(1).get());
        assertFalse(values.get(2).isPresent());
    }

    @Test
    public void testObserveUserLoggedOut() throws Exception {
        FirebaseUser firebaseUser1 = mock(FirebaseUser.class);

        FirebaseAuth firebaseAuth1 = createFirebaseAuth(firebaseUser1);
        FirebaseAuth firebaseAuth2 = createFirebaseAuth(null);

        final TestObserver<Maybe<FirebaseUser>> observer = target.observeUserLoggedOut().test();
        authStateListener.onAuthStateChanged(firebaseAuth1);
        authStateListener.onAuthStateChanged(firebaseAuth2);

        observer.assertValueCount(1);
        final List<Maybe<FirebaseUser>> values = observer.values();
        assertFalse(values.get(0).isPresent());
    }

    @Test
    public void testObserveUserLoggedIn() throws Exception {
        FirebaseUser firebaseUser1 = mock(FirebaseUser.class);

        FirebaseAuth firebaseAuth1 = createFirebaseAuth(firebaseUser1);
        FirebaseAuth firebaseAuth2 = createFirebaseAuth(null);

        final TestObserver<FirebaseUser> observer = target.observeUserLoggedIn().test();
        authStateListener.onAuthStateChanged(firebaseAuth1);
        authStateListener.onAuthStateChanged(firebaseAuth2);

        observer.assertValues(firebaseUser1);
    }

    private FirebaseAuth createFirebaseAuth(@Nullable FirebaseUser firebaseUser) {
        final FirebaseAuth firebaseAuth = mock(FirebaseAuth.class);
        when(firebaseAuth.getCurrentUser()).thenReturn(firebaseUser);
        return firebaseAuth;
    }
}