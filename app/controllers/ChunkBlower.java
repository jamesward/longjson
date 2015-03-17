package controllers;

import akka.actor.Cancellable;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Results;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class ChunkBlower extends Results.StringChunks {

    private final int secondsBetweenChunks;
    private final F.Promise<String> job;

    public ChunkBlower(int secondsBetweenChunks, F.Promise<String> job) {
        this.secondsBetweenChunks = secondsBetweenChunks;
        this.job = job;
    }

    @Override
    public void onReady(final Out<String> out) {

        Cancellable tick = Akka.system().scheduler().schedule(
                Duration.Zero(),
                Duration.create(secondsBetweenChunks, TimeUnit.SECONDS),
                (Runnable) () -> out.write(" "),
                Akka.system().dispatcher()
        );

        job.onRedeem(output -> {
            out.write(output);
            tick.cancel();
            out.close();
        });
    }

}
