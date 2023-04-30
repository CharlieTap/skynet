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
public class SkynetProjectDependency extends DelegatingProjectDependency {

    @Inject
    public SkynetProjectDependency(TypeSafeProjectDependencyFactory factory, ProjectDependencyInternal delegate) {
        super(factory, delegate);
    }

    /**
     * Creates a project dependency on the project at path ":bin"
     */
    public BinProjectDependency getBin() { return new BinProjectDependency(getFactory(), create(":bin")); }

    /**
     * Creates a project dependency on the project at path ":lib"
     */
    public LibProjectDependency getLib() { return new LibProjectDependency(getFactory(), create(":lib")); }

}
