package calculator2.calculator;

import calculator2.calculator.helpers.Helper;
import calculator2.calculator.util.actions.Sign;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static calculator2.calculator.Element.ElementType.*;

public class Parser<T> {
    private final StringBuilder sb;
    private final Helper<T> helper;
    private Stack<Element> stack;
    private final List<String> varNames;
    private final Stack<Integer> ints;
    private final Stack<String> bracketCheck;

    public Parser(Helper<T> helper) {
        this.helper = helper;
        sb = new StringBuilder();
        varNames = new Stack<>();
        ints = new Stack<>();
        bracketCheck = new Stack<>();
    }

    public int parse(String str) {
        str = str.replaceAll("[ \\n\\t]", "");
        List<Element> list = toList(str);
        normaliseBrackets(list);
        normaliseLetters(list);
        findUnarySigns(list);
        addMissingSigns(list);
        int size = calculateVars(list);
        this.stack = toStack(list);
        return size;
    }

    private Element.ElementType type(char c) {
        Element.ElementType type;
        if (helper.isType(c)) {
            type = Element.ElementType.NUMBER;
        } else if (helper.brackets.isBracket(c)) {
            type = Element.ElementType.BRACKET;
        } else if (helper.getSign(c) != null) {
            type = Element.ElementType.SIGN;
        } else if (helper.isDivider(c)) {
            type = Element.ElementType.DIVIDER;
        } else {
            type = FUNCTION;
        }
        return type;
    }

    private List<Element> toList(String str) {
        List<Element> list = new ArrayList<>();
        if (str.isEmpty())
            str = "0";
        Element.ElementType lt;
        sb.append(str.charAt(0));
        lt = type(str.charAt(0));
        for (int i = 1; i < str.length(); ++i) {
            char c = str.charAt(i);
            Element.ElementType type = type(c);
            if (lt != type || type == Element.ElementType.BRACKET || type == Element.ElementType.SIGN) {
                Element e = new Element(sb.toString(), lt);
                sb.setLength(0);
                list.add(e);
            }
            lt = type;
            sb.append(c);
        }
        Element e = new Element(sb.toString(), lt);
        sb.setLength(0);
        list.add(e);
        return list;
    }

    private void normaliseBrackets(List<Element> list) {
        for (int i = 0; i < list.size(); ++i) {
            Element e = list.get(i);
            if (e.type == BRACKET) {
                if (helper.brackets.brOpens(e.symbol)) {
                    bracketCheck.push(helper.brackets.getOpposit(e.symbol));
                } else {
                    if (bracketCheck.empty()) {
                        list.add(0, new Element(helper.brackets.getOpposit(e.symbol), BRACKET));
                        ++i;
                    } else if (!bracketCheck.pop().equals(e.symbol)) {
                        throw new RuntimeException(CalcLanguage.PARSER_ERRORS[5] + " " + CalcLanguage.PARSER_ERRORS[3]);
                    }
                }
            }
        }
        while (!bracketCheck.empty()) {
            list.add(new Element(bracketCheck.pop(), BRACKET));
        }
    }

    private void normaliseLetters(List<Element> list) {
        for (int i = list.size() - 1; i >= 0; --i) {
            Element e = list.get(i);
            if (e.type == FUNCTION) {
                if (!helper.hasName(e.symbol)) {
                    list.remove(i);
                    for (int j = e.symbol.length() - 1; j >= 0; --j) {
                        String s = e.symbol.substring(j);
                        if (helper.hasName(s)) {
                            Element nE = new Element(s,
                                    helper.isVar(s) ? VAR :
                                            helper.isConst(s) ? CONSTANT :
                                                    helper.isLambda(s) ? LAMBDA :
                                                            FUNCTION);
                            list.add(i, nE);
                            e.symbol = e.symbol.substring(0, j);
                        }
                    }
                    if (e.symbol.length() != 0) {
                        throw new RuntimeException(CalcLanguage.PARSER_ERRORS[0] + " '" + e.symbol + "'");
                    }
                } else if (helper.isVar(e.symbol)) {
                    e.type = VAR;
                } else if (helper.isConst(e.symbol)) {
                    e.type = CONSTANT;
                } else if (helper.isLambda(e.symbol)) {
                    e.type = LAMBDA;
                }
            }
        }
    }

    private void findUnarySigns(List<Element> list) {
        Element last = list.get(0);
        if (last.type == SIGN && helper.getSign(last.symbol).canBeUnary) {
            last.type = FUNCTION;
        }
        for (int i = 1; i < list.size(); ++i) {
            Element e = list.get(i);
            if (e.type == SIGN && helper.getSign(e.symbol).canBeUnary &&
                    (last.type == BRACKET && helper.brackets.brOpens(last.symbol)
                            || last.type == DIVIDER
                            || last.type == SIGN)) {
                e.type = FUNCTION;
            }
            last = e;
        }
    }

    private void addMissingSigns(List<Element> list) {
        Sign<T> sign = helper.getMissingSign();
        if (sign == null)
            return;
        Element last = list.get(list.size() - 1);
        for (int i = list.size() - 2; i >= 0; --i) {
            Element element = list.get(i);
            boolean needSign = false;
            switch (element.type) {
                case BRACKET:
                    if (helper.brackets.brOpens(element.symbol))
                        break;
                case CONSTANT:
                case VAR:
                case NUMBER:
                    needSign = last.type == CONSTANT || last.type == FUNCTION || last.type == NUMBER || last.type == VAR || openBracket(last);
                    break;
            }
            if (needSign) {
                list.add(i + 1, new Element(String.valueOf(sign.name), SIGN));
            }
            last = element;
        }
    }

    private Stack<Element> toStack(List<Element> list) {
        Stack<Element> stack = new Stack<>();
        while (!list.isEmpty()) {
            stack.push(list.remove(list.size() - 1));
        }
        return stack;
    }

    public void simpleCheck(List<Element> stack) {
        int returns = 0;
        if (!ints.empty())
            ints.clear();
        for (int i = 0; i < stack.size(); ++i) {
            Element e = stack.get(i);
            switch (e.type) {
                case SIGN:
                    returns -= 1;
                    break;
                case LAMBDA:
                    if (i > 0 && stack.get(i - 1).type == BRACKET && helper.brackets.brOpens(stack.get(i - 1).symbol)) {
                        returns += 2;
                    } else {
                        returns += 1;
                    }
                    break;
                case FUNCTION:
                    int args = helper.getFunc(e.symbol).args;
                    returns -= args;
                    if ((args > 1 || args == -1)
                            && (i == 0
                            || stack.get(i - 1).type != BRACKET
                            || !helper.brackets.brOpens(stack.get(i - 1).symbol))) {
                        throw new RuntimeException(CalcLanguage.PARSER_ERRORS[5] + " " + e.symbol);
                    }
                case VAR:
                case NUMBER:
                case CONSTANT:
                    returns++;
                    break;
                case BRACKET:
                    if (!helper.brackets.brOpens(e.symbol)) {
                        ints.push(returns);
                        returns = 0;
                    } else {
                        if (i < stack.size() - 1) {
                            Element e1 = stack.get(i + 1);
                            if (e1.type == FUNCTION || e1.type == LAMBDA) {
                                int needArgs;
                                if (e1.type == FUNCTION) {
                                    needArgs = helper.
                                            getFunc(e1.symbol).args;
                                } else {
                                    needArgs = -1;
                                }
                                if (needArgs != -1) {
                                    if (returns < needArgs)
                                        throw new RuntimeException(CalcLanguage.PARSER_ERRORS[1] + " " + CalcLanguage.PARSER_ERRORS[4] + " " + e1.symbol);
                                    else if (returns > needArgs)
                                        throw new RuntimeException(CalcLanguage.PARSER_ERRORS[2] + " " + CalcLanguage.PARSER_ERRORS[4] + " " + e1.symbol);
                                }else{
                                    e.symbol += returns;
                                }
                                returns = ints.pop() + needArgs;
                                break;
                            }
                        }
                        if (returns < 1)
                            throw new RuntimeException(CalcLanguage.PARSER_ERRORS[1] + " " + CalcLanguage.PARSER_ERRORS[4] + " " + CalcLanguage.PARSER_ERRORS[3]);
                        else if (returns > 1)
                            throw new RuntimeException(CalcLanguage.PARSER_ERRORS[2] + " " + CalcLanguage.PARSER_ERRORS[4] + " " + CalcLanguage.PARSER_ERRORS[3]);
                        returns = ints.pop() + 1;
                    }
            }
        }
        if (returns < 1)
            throw new RuntimeException(CalcLanguage.PARSER_ERRORS[1]);
        else if (returns > 1)
            throw new RuntimeException(CalcLanguage.PARSER_ERRORS[2]);
    }

    private int calculateVars(List<Element> list) {
        int level = 0;
        varNames.clear();
        for (Element element : list) {
            if (element.type == BRACKET && helper.brackets.brLambda(element.symbol)) {
                if (helper.brackets.brOpens(element.symbol)) {
                    ++level;
                } else {
                    --level;
                }
            } else if ((element.type == VAR || element.type == LAMBDA) && level == 0) {
                String name = element.symbol;
                if (!varNames.contains(name)) {
                    varNames.add(name);
                }
            }
        }
        return varNames.size();
    }

    private boolean openBracket(Element e) {
        return e.type == BRACKET && helper.brackets.brOpens(e.symbol);
    }

    public Stack<Element> getStack() {
        return stack;
    }
}
