package org.jvnet.hudson.plugins.exclusion;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Executor;
import hudson.tasks.Builder;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author Anthony Roux
 **/
public class CriticalBlockStart extends Builder {

    public static IdAllocator pa;
    public static IdAllocationManager pam = null;

    @DataBoundConstructor
    public CriticalBlockStart() {
    }

    //Methode appell� lors du step Critical Block Start
    //
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
    //    List<RessourcesMonitor> listRessources = IdAllocator.getListRessources();

        final Computer cur = Executor.currentExecutor().getOwner();
        pam = IdAllocationManager.getManager(cur);

    /*    for (RessourcesMonitor rm : listRessources) {
            if (build.getProject().getName().equals(rm.getJobName())) {
                rm.setBuild(true);
                rm.setAbsBuild(build);
                rm.setLauncher(launcher);
                rm.setListener(listener);
            }
        }
        IdAllocator.setListRessources(listRessources);

        */
        //Init Builder
        PrintStream logger = listener.getLogger();


        //Liste des IDs utilis�es
        //
        final List<Id> allocated = new ArrayList<Id>();

        //On verifie qu'on a bien coch� le plugin
        // & qu'on a des IDs  � allouer
        // A TESTER : Enlever le pa != null
        if (pa != null && pa.ids != null && pa.isActivated) {
            //Pour chaque ID du projet
            //
            for (IdType pt : pa.ids) {
                logger.println("Allocating Id : " + pt.name);

                //On alloue l'id :
                // isActivated � faux car on lancer le job
                // Attendre tant que l'id est utilis�
                //Quand fini on sajoute nous meme dans le dico pour dire les IDs qu'on utilise
                //c'est la m�thode synchronized

                Id p = pt.allocate(true, build, pam, launcher, listener);

                List<RessourcesMonitor> listR = IdAllocator.getListRessources();

                for (RessourcesMonitor rm : listR) {
                    if (build.getProject().getName().equals(rm.getJobName()) && p.type.name.equals(rm.getRessource())) {
                        rm.setBuild(true);
                        rm.setAbsBuild(build);
                        rm.setLauncher(launcher);
                        rm.setListener(listener);
                    }
                }
                
                IdAllocator.setListRessources(listR);
                //On ajoute dans allocate les IDs utilise
                allocated.add(p);

                logger.println("  -> Assigned " + p.get());
            }
            logger.println("Resource allocation complete");
        }
        return true;
    }

    public String getDisplayName() {
        return "Critical block start";
    }
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends Descriptor<Builder> {

        DescriptorImpl() {
            super(CriticalBlockStart.class);
            load();
        }

        public String getDisplayName() {
            return "Critical block start";
        }
    }
}
