package cn.edu.fudan.codetracker.aop;

import cn.edu.fudan.codetracker.component.RestInterfaceManager;
import cn.edu.fudan.codetracker.domain.RepoResourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * description:
 *
 * @author fancying
 * create: 2020-04-22 16:02
 **/
@Aspect
@Component
@Slf4j
public class ReleaseResourceAspect {

    private RestInterfaceManager restInvoker;

    // 定义切点

    @Pointcut("@annotation(cn.edu.fudan.codetracker.annotation.FreeResource)")
    public void release() {
    }

    // 定义执行操作

    @AfterReturning("release()")
    public void releaseRepoRelease(JoinPoint joinPoint) {
        RepoResourceDTO repoResourceDTO = new RepoResourceDTO();
        for (Object o : joinPoint.getArgs()) {
            if (o instanceof RepoResourceDTO) {
                repoResourceDTO = (RepoResourceDTO)o;
            }
        }
        log.info("free repo:{}, path:{}", repoResourceDTO.getRepoId(), repoResourceDTO.getRepoPath());
        restInvoker.freeRepo(repoResourceDTO.getRepoId(), repoResourceDTO.getRepoPath());
    }

    @Autowired
    public void setRestInvoker(RestInterfaceManager restInvoker) {
        this.restInvoker = restInvoker;
    }
}