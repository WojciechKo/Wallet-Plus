package pl.net.korzeniowski.walletplus.service.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import pl.net.korzeniowski.walletplus.WalletPlus;
import pl.net.korzeniowski.walletplus.model.Profile;
import pl.net.korzeniowski.walletplus.service.ProfileService;
import pl.net.korzeniowski.walletplus.util.PrefUtils;

@Aspect
public class DatabaseChangerAspect {
    public static final String LOGGER_CLASS_NAME = "pl.net.korzeniowski.walletplus.service.aspect.DatabaseChanger";

    private static final String POINTCUT_METHOD =
            "execution(@" + LOGGER_CLASS_NAME + " * *(..))";
    private static final String POINTCUT_CONSTRUCTOR =
            "execution(@" + LOGGER_CLASS_NAME + " *.new(..))";

    @Pointcut(POINTCUT_METHOD)
    public void methodAnnotatedWithDebugTrace() {
    }

    @Pointcut(POINTCUT_CONSTRUCTOR)
    public void constructorAnnotatedDebugTrace() {
    }

    @Around("methodAnnotatedWithDebugTrace() || constructorAnnotatedDebugTrace()")
    public Object weaveJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        ProfileService profileService = WalletPlus.getInstance().component().profileService();

        profileService.update(profileService.getActiveProfile().setSynchronized(false));
        return result;
    }
}