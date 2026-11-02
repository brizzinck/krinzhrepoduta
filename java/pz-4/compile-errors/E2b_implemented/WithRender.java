import ua.duikt.lavrishko.pr4.exp1.Document;

// Той самий клас з реалізацією render(): компілюється
class WithRender extends Document {
    WithRender() {
        super("Назва");
    }

    @Override
    public String render() {
        return "текст";
    }
}
