package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.internal.artifacts.dependencies.ProjectDependencyInternal;
import org.gradle.api.internal.artifacts.DefaultProjectDependencyFactory;
import org.gradle.api.internal.artifacts.dsl.dependencies.ProjectFinder;
import org.gradle.api.internal.catalog.DelegatingProjectDependency;
import org.gradle.api.internal.catalog.TypeSafeProjectDependencyFactory;
import javax.inject.Inject;

@NonNullApi
public class RootProjectAccessor extends TypeSafeProjectDependencyFactory {


    @Inject
    public RootProjectAccessor(DefaultProjectDependencyFactory factory, ProjectFinder finder) {
        super(factory, finder);
    }

    /**
     * Creates a project dependency on the project at path ":"
     */
    public SkynetProjectDependency getSkynet() { return new SkynetProjectDependency(getFactory(), create(":")); }

    /**
     * Creates a project dependency on the project at path ":lib"
     */
    public LibProjectDependency getLib() { return new LibProjectDependency(getFactory(), create(":lib")); }

    /**
     * Creates a project dependency on the project at path ":program"
     */
    public ProgramProjectDependency getProgram() { return new ProgramProjectDependency(getFactory(), create(":program")); }

}
