package ge.transitgeorgia;

import androidx.hilt.work.HiltWorkerFactory;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class App_MembersInjector implements MembersInjector<App> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public App_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<App> create(Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new App_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(App instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("ge.transitgeorgia.App.workerFactory")
  public static void injectWorkerFactory(App instance, HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
