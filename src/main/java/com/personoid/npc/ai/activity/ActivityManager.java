package com.personoid.npc.ai.activity;

import com.personoid.npc.NPC;
import com.personoid.npc.components.NPCTickingComponent;
import com.personoid.utils.debug.Profiler;
import org.bukkit.Bukkit;

import java.util.*;

public class ActivityManager extends NPCTickingComponent {
    private final Set<Activity> registered = new HashSet<>();
    private final Queue<Activity> queue = new PriorityQueue<>(Collections.reverseOrder());
    private final Set<Activity> paused = new HashSet<>();
    public Activity current;

    public HashMap<Activity, Integer> boredTasks = new HashMap<>();

    public ActivityManager(NPC npc) {
        super(npc);
    }

    public void register(Activity... activities) {
        registered.addAll(Arrays.asList(activities));
    }

    public void register(Set<Activity> activities) {
        registered.addAll(activities);
    }

    public void register(List<Activity> activities) {
        registered.addAll(activities);
    }

    @Override
    public void tick() {
        for (Activity activity : boredTasks.keySet()){
            boredTasks.put(activity, boredTasks.get(activity)-1);
            if (boredTasks.get(activity) <= 0){
                boredTasks.remove(activity);
                Profiler.push(Profiler.Type.ACTIVITY_MANAGER, "Removed bored cooldown for: " + activity.getClass().getSimpleName());
            }
        }
        if (current != null) {
            //Bukkit.broadcastMessage(current.getClass().getSimpleName() + " is ticking");
            if (current.isFinished()) {
                current = null;
            } else if (!queue.isEmpty() && queue.peek().getPriority().isHigherThan(current.getPriority()) && current.canStop(Activity.StopType.PAUSE)) {
                current.internalStop(Activity.StopType.PAUSE);
                current.onStop(Activity.StopType.PAUSE);
                current.setPaused(true);
                paused.add(current);
            } else {
                current.internalUpdate();
                current.onUpdate();
            }
        } else startNextActivity();
    }

    public void startNextActivity() {
        //Bukkit.broadcastMessage("Starting next activity");
        if (!paused.isEmpty()) {
            for (Activity activity : paused) {
                boolean higherThanNext = queue.isEmpty() || activity.getPriority().isHigherThan(queue.peek().getPriority());
                activity.setManager(this);
                if (higherThanNext && activity.canStart(Activity.StartType.RESUME)) {
                    startActivity(activity, Activity.StartType.RESUME);
                    Bukkit.broadcastMessage("attempt start paused true");
                    return;
                }
            }
        }
        queueIfEmpty();
        Activity next = queue.poll();
        if (next != null) {
            next.setManager(this);
            if (next.canStart(Activity.StartType.START)) {
                startActivity(next, Activity.StartType.START);
            }
        }
    }

    public void startActivity(Activity activity, Activity.StartType startType) {
        Bukkit.broadcastMessage("Starting activity " + activity.getClass().getSimpleName());
        activity.internalStart(startType);
        activity.onStart(startType);
        if (startType == Activity.StartType.RESUME) {
            activity.setPaused(false);
            paused.remove(activity);
        }
        current = activity;
    }

    public void queueActivity(Activity activity) {
        queue.add(activity);
    }

    public void queueIfEmpty() {
        if (queue.isEmpty()) {
            Activity chosen = chooseViaPriority();
            if (chosen != null) queueActivity(chosen);
        }
    }

    public Activity chooseViaPriority() {
        Set<Activity> canStart = new HashSet<>();

        // Get all goals that can activate.
        for (Activity activity : registered){
            if (boredTasks.containsKey(activity)) continue;
            if (current != null){
                if (!activity.getPriority().isHigherThan(current.getPriority()) || activity.getPriority() != current.getPriority()){
                    continue;
                }
            }
            activity.setManager(this);
            if (activity.canStart(Activity.StartType.START)){
                Profiler.push(Profiler.Type.ACTIVITY_MANAGER, "can start " + activity.getClass().getSimpleName());
                canStart.add(activity);
            }
        }

        Activity highest = null;

        for (Activity activity : canStart){
            if (highest == null || activity.getPriority().isHigherThan(highest.getPriority())){
                highest = activity;
            }
        }

        if (highest != null) {
            Profiler.push(Profiler.Type.ACTIVITY_MANAGER, "highest priority activity " + highest.getClass().getSimpleName());
            if (current != null) {
                if (highest.getPriority() == current.getPriority()) {
                    if (random.nextBoolean()){
                        Profiler.push(Profiler.Type.ACTIVITY_MANAGER, "select activity random " + highest.getClass().getSimpleName());
                        return highest;
                    }
                }
            }
            else {
                Profiler.push(Profiler.Type.ACTIVITY_MANAGER, "select activity " + highest.getClass().getSimpleName());
                return highest;
            }
        }
        return null;
    }
}
