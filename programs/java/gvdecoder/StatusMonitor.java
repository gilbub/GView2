package gvdecoder;

/** Interface to allow a thread to be monitored and cancelled.
    Gui elements should define a progress bar and cancel button
    or similar that are called by isCancelled() and setProgress()
    functions. Note, setProgress takes a value between 0 and 1.
 **/
public interface StatusMonitor{

  public void showProgress(double val);
  public boolean isCancelled();

}