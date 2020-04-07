package controller;

import framesLib.MyFrame;
import framesLib.TextPanel;

import view.elements.TextElement;
import view.grapher.graphics.Function;
import view.grapher.graphics.Implicit;
import view.grapher.graphics.Parametric;
import view.grapher.graphics.Translation;
import view.support_panels.*;

public class SupportFrameManager {
    private MyFrame frame;
    private final FunctionSettings functionSettings;
    private final ParametricSettings parametricSettings;
    private final ImplicitSettings implicitSettings;
    private final TranslationSettings translationSettings;
    private final TimerSettings timerSettings;
    private final HelperFrame helperFrame;
    private final MainSettings mainSettings;
    SupportFrameManager(ModelUpdater updater){
        functionSettings = new FunctionSettings(updater);
        parametricSettings = new ParametricSettings(updater);
        timerSettings = new TimerSettings(updater);
        implicitSettings = new ImplicitSettings(updater);
        translationSettings = new TranslationSettings(updater);
        helperFrame = new HelperFrame(updater);
        mainSettings = new MainSettings(updater);
    }
    void openFunctionSettings(Function f, TextElement e){
        checkFrame();
        functionSettings.setInfo(f, e);
        frame.changeScreen(functionSettings);
        setOnTop();
    }
    void openParameterSettings(Parametric p, TextElement e){
        checkFrame();
        parametricSettings.setInfo(p, e);
        frame.changeScreen(parametricSettings);
        setOnTop();
    }
    void openImplicitSettings(Implicit imp, TextElement e){
        checkFrame();
        implicitSettings.setInfo(imp, e);
        frame.changeScreen(implicitSettings);
        setOnTop();
    }
    void openTranslationSettings(Translation tr, TextElement e){
        checkFrame();
        translationSettings.setInfo(tr, e);
        frame.changeScreen(translationSettings);
        setOnTop();
    }
    void openTimerSettings(){
        checkFrame();
        frame.changeScreen(timerSettings);
        setOnTop();
    }
    void openUpdaterFrame(VersionController.UpdateInfo info){
        checkFrame();
        frame.changeScreen(new UpdaterFrame(info));
        setOnTop();
    }
    public void openHelperFrame(){
        checkFrame();
        frame.changeScreen(helperFrame);
        setOnTop();
    }
    public void openTextFrame(TextPanel panel){
        if(frame == null || !frame.isVisible())
            frame = new MyFrame(true);
        frame.changeScreen(panel);
        frame.setFocusable(true);
    }
    public void openMainSettings(){
        checkFrame();
        frame.changeScreen(mainSettings);
        frame.setFocusable(true);
    }
    private void setOnTop(){
        frame.setFocusable(true);
        frame.setFocusableWindowState(true);
    }
    void close(){
        if(frame != null && frame.isVisible())
            frame.dispose();
    }
    private void checkFrame(){
        if(frame == null || !frame.isVisible())
            frame = new MyFrame(true);
        else {
            frame.clearStack();
        }
    }
    Double getTime(){
        return timerSettings.getT();
    }
    public TimerSettings getTimer(){
        return timerSettings;
    }

    public MainSettings getMainSettings() {
        return mainSettings;
    }

    public void updateLanguage(){
        helperFrame.updateLanguage();
        mainSettings.updateLanguage();
        functionSettings.updateLanguage();
        parametricSettings.updateLanguage();
        implicitSettings.updateLanguage();
        translationSettings.updateLanguage();
        timerSettings.updateLanguage();
    }
}
